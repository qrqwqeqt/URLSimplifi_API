package com.linkurlshorter.urlshortener.link;

import com.linkurlshorter.urlshortener.link.dto.LinkInfoDto;
import com.linkurlshorter.urlshortener.link.dto.LinkInfoDtoMapper;
import com.linkurlshorter.urlshortener.link.dto.LinkStatisticsDto;
import com.linkurlshorter.urlshortener.link.exception.ForbiddenException;
import com.linkurlshorter.urlshortener.link.exception.InternalServerLinkException;
import com.linkurlshorter.urlshortener.link.exception.LinkStatusException;
import com.linkurlshorter.urlshortener.link.generator.ShortLinkGenerator;
import com.linkurlshorter.urlshortener.link.model.Link;
import com.linkurlshorter.urlshortener.link.model.LinkStatus;
import com.linkurlshorter.urlshortener.link.request.CreateLinkRequest;
import com.linkurlshorter.urlshortener.link.request.EditLinkContentRequest;
import com.linkurlshorter.urlshortener.link.response.CreateLinkResponse;
import com.linkurlshorter.urlshortener.link.response.LinkInfoResponse;
import com.linkurlshorter.urlshortener.link.response.LinkModifyingResponse;
import com.linkurlshorter.urlshortener.link.response.LinkStatisticsResponse;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Controller for Link-related operations such as create, delete, update and get info + statistics
 *
 * @author Artem Poliakov
 * @version 1.0
 */
@RestController
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Link", description = "The Link API")
@RequestMapping("/api/V1/link")
public class LinkController {
    private static final int SHORT_LINK_LIFETIME_IN_DAYS = 30;
    private static final String OPERATION_FORBIDDEN_MSG = "Operation forbidden!";

    private final LinkService linkService;
    private final UserService userService;
    private final LinkInfoDtoMapper linkDtoMapper;
    private final ShortLinkGenerator linkGenerator;

    /**
     * Controller method for creating a new link.
     * <p>
     * This method handles POST requests to create a new link. It accepts a valid {@link CreateLinkRequest}
     * in the request body and generates a new short URL for the provided long URL. The short URL is then
     * associated with the authenticated user and stored in the database. Upon successful creation of the link,
     * a {@link CreateLinkResponse} containing the newly generated short URL is returned with a status of 200 (OK).
     * If any errors occur during the process, an {@link InternalServerLinkException} is thrown.
     * </p>
     *
     * @param createRequest the request object containing the long URL to be shortened
     * @return a ResponseEntity containing the response object indicating the success or failure of the link creation,
     * along with the generated short URL
     * @throws InternalServerLinkException if an error occurs during the link creation process
     * @see CreateLinkRequest
     * @see CreateLinkResponse
     * @see InternalServerLinkException
     */
    @PostMapping("/create")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Create new link")
    public ResponseEntity<CreateLinkResponse> createLink(@RequestBody @Valid CreateLinkRequest createRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByEmail(authentication.getName());
        String newShortUrl;
        if (Objects.nonNull(createRequest.getShortLinkName())) {
            newShortUrl = createRequest.getShortLinkName();
        } else {
            newShortUrl = linkGenerator.generate();
        }
        try {
            linkService.save(
                    Link.builder()
                            .longLink(createRequest.getLongLink())
                            .shortLink(newShortUrl)
                            .expirationTime(LocalDateTime.now().plusDays(SHORT_LINK_LIFETIME_IN_DAYS))
                            .user(user)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalServerLinkException();
        }
        return ResponseEntity.ok(new CreateLinkResponse("ok", newShortUrl));
    }

    /**
     * Handles a request to delete a link.
     *
     * @param shortLink the String short link of the link to delete
     * @return a ResponseEntity containing the response object indicating the success of the deletion operation
     * @throws ForbiddenException if the authenticated user does not have rights to delete the link
     */
    @PostMapping("/delete")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Delete link by short link")
    public ResponseEntity<LinkModifyingResponse> deleteLink(@RequestParam String shortLink) {
        if (doesUserHaveRightsForLinkByShortLink(shortLink)) {
            linkService.deleteByShortLink(shortLink);
            return ResponseEntity.ok(new LinkModifyingResponse("ok"));
        } else {
            throw new ForbiddenException(OPERATION_FORBIDDEN_MSG);
        }
    }

    /**
     * Handles a request to edit the content of a link.
     *
     * @param request the request object containing the old short link of the link and the new short link
     * @return a ResponseEntity containing the response object indicating the success of the edit operation
     * @throws ForbiddenException  if the authenticated user does not have rights to edit the link
     * @throws LinkStatusException if the status of the link is not ACTIVE
     */
    @PostMapping("/edit/content")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Edit link content")
    public ResponseEntity<LinkModifyingResponse> editLinkContent(@RequestBody @Valid EditLinkContentRequest request) {
        if (doesUserHaveRightsForLinkByShortLink(request.getOldShortLink())) {
            Link link = linkService.findByShortLink(request.getOldShortLink());
            if (link.getStatus() != LinkStatus.ACTIVE) {
                throw new LinkStatusException();
            }
            link.setShortLink(request.getNewShortLink());
            linkService.updateRedisShortLink(request.getOldShortLink(), request.getNewShortLink());
            linkService.update(link);
            return ResponseEntity.ok(new LinkModifyingResponse("ok"));
        } else {
            throw new ForbiddenException(OPERATION_FORBIDDEN_MSG);
        }
    }

    /**
     * Handles a request to refresh the expiration time of a link.
     *
     * @param shortLink the String shortLink of the link to refresh
     * @return a ResponseEntity containing the response object indicating the success of the refresh operation
     * @throws ForbiddenException   if the authenticated user does not have rights to refresh the link
     */
    @PostMapping("/edit/refresh")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Refresh link expiration time")
    public ResponseEntity<LinkModifyingResponse> refreshLink(@RequestParam String shortLink) {
        if (doesUserHaveRightsForLinkByShortLink(shortLink)) {
            Link link = linkService.findByShortLink(shortLink);
            link.setExpirationTime(LocalDateTime.now().plusDays(SHORT_LINK_LIFETIME_IN_DAYS));
            link.setStatus(LinkStatus.ACTIVE);
            linkService.updateRedisLink(shortLink, link);
            linkService.update(link);
            return ResponseEntity.ok(new LinkModifyingResponse("ok"));
        } else {
            throw new ForbiddenException(OPERATION_FORBIDDEN_MSG);
        }
    }

    /**
     * Retrieves information about a link using its short link.
     *
     * @param shortLink the short link of the link to retrieve information for
     * @return a ResponseEntity containing the response object with information about the link
     * @throws ForbiddenException if the authenticated user does not have rights to access the link
     */
    @GetMapping("/info")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get link info")
    public ResponseEntity<LinkInfoResponse> getInfoByShortLink(@RequestParam String shortLink) {
        if (doesUserHaveRightsForLinkByShortLink(shortLink)) {
            Link link = linkService.findByShortLink(shortLink);
            LinkInfoDto dto = linkDtoMapper.mapLinkToDto(link);
            LinkInfoResponse response = new LinkInfoResponse(List.of(dto), "ok");
            return ResponseEntity.ok(response);
        } else {
            throw new ForbiddenException(OPERATION_FORBIDDEN_MSG);
        }
    }

    /**
     * Retrieves information about all links associated with the authenticated user.
     *
     * @return a ResponseEntity containing the response object with information about all links for the user
     */
    @GetMapping("/all-links-info")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get all User's links info")
    public ResponseEntity<LinkInfoResponse> getAllLinksForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID requesterUserId = userService.findByEmail(authentication.getName()).getId();
        List<LinkInfoDto> linksDto = linkService
                .findAllByUserId(requesterUserId)
                .stream()
                .map(linkDtoMapper::mapLinkToDto)
                .sorted(Comparator.comparing(LinkInfoDto::getUsageStatistics).reversed())
                .toList();
        return ResponseEntity.ok(new LinkInfoResponse(linksDto, "ok"));
    }

    /**
     * Retrieves a list of active links associated with the currently authenticated user.
     * This method first fetches the user ID of the currently authenticated user using the UserService.
     * Then, it calls the LinkService to retrieve all active links associated with the user identified by the user ID.
     *
     * @return A list of active LinkDto objects associated with the currently authenticated user.
     */
    @GetMapping("/active-links")
    public List<LinkInfoDto> getOnlyActiveLinks(){
        UUID userId = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        return linkService
                .findAllActiveByUserId(userId)
                .stream()
                .map(linkDtoMapper::mapLinkToDto)
                .toList();
    }

    /**
     * Retrieves usage statistics for all links associated with the authenticated user.
     *
     * @return a ResponseEntity containing the response object with usage statistics for all links for the user,
     * links are sorted in descending order
     */
    @GetMapping("/url-usage-top-for-user")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get all links usage statistics")
    public ResponseEntity<LinkStatisticsResponse> getLinksStatsForUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User requesterUser = userService.findByEmail(authentication.getName());
        List<LinkStatisticsDto> stats = linkService.getLinkUsageStatsByUserId(requesterUser.getId());
        stats.sort(Comparator.comparing(LinkStatisticsDto::getUsageStatistics).reversed());
        return ResponseEntity.ok(new LinkStatisticsResponse(stats, "ok"));
    }

    /**
     * Checks if the authenticated user has rights to perform operations on a given link.
     *
     * @param shortLink the String short link of the link to check
     * @return true if the user has rights, false otherwise
     */
    private boolean doesUserHaveRightsForLinkByShortLink(String shortLink) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID linkUserId = linkService.findByShortLink(shortLink).getUser().getId();
        UUID currentUserId = userService.findByEmail(authentication.getName()).getId();
        return linkUserId.equals(currentUserId);
    }
}

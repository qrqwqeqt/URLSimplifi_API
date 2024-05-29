package com.linkurlshorter.urlshortener.link;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkurlshorter.urlshortener.TestConfig;
import com.linkurlshorter.urlshortener.link.dto.LinkStatisticsDto;
import com.linkurlshorter.urlshortener.link.model.Link;
import com.linkurlshorter.urlshortener.link.model.LinkStatus;
import com.linkurlshorter.urlshortener.link.request.CreateLinkRequest;
import com.linkurlshorter.urlshortener.link.request.EditLinkContentRequest;
import com.linkurlshorter.urlshortener.security.SecurityConfig;
import com.linkurlshorter.urlshortener.user.UserService;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link LinkController} class.
 *
 * @author Anastasiia Usenko
 */
@WebMvcTest(controllers = LinkController.class)
@Import({SecurityConfig.class, TestConfig.class})
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LinkService linkService;

    @MockBean
    private UserService userService;


    private User user;
    private Link link;

    /**
     * Set up method to initialize test data before each test method.
     */
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c81"))
                .email("test1@gmail.com")
                .password("Password1")
                .role(UserRole.USER)
                .build();

        link = Link.builder()
                .id(UUID.fromString("3053e49b-6da3-4389-9d06-23b2d57b6f25"))
                .longLink("https://www.youtube.com")
                .shortLink("shortLink1")
                .user(user)
                .createdTime(LocalDateTime.of(2024, 4, 13, 10, 0))
                .expirationTime(LocalDateTime.of(2024, 5, 16, 8, 0))
                .statistics(100)
                .status(LinkStatus.ACTIVE)
                .build();
    }

    /**
     * Test case for the {@link LinkController#createLink(CreateLinkRequest)} method when the short link is provided.
     */
    @Test
    @WithMockUser
    void createLinkTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.save(any())).thenReturn(link);

        CreateLinkRequest request = new CreateLinkRequest("https://www.example.com", "example");

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"))
                .andExpect(jsonPath("$.shortLink").value("example"));
    }

    /**
     * Test case for the {@link LinkController#createLink(CreateLinkRequest)} method when the short link is not provided.
     */
    @Test
    @WithMockUser
    void createLinkNotProvidedShortLinkTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.save(any())).thenReturn(link);

        CreateLinkRequest request = new CreateLinkRequest("https://www.example.com", null);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"))
                .andExpect(jsonPath("$.shortLink").exists());
    }

    /**
     * Test case for the {@link LinkController#createLink(CreateLinkRequest)} method when
     * an error occurs during the link creation process.
     */
    @Test
    @WithMockUser
    void createLinkFailedTest() throws Exception {
        CreateLinkRequest request = new CreateLinkRequest("https://www.example.com", "example");

        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.save(any())).thenThrow(new RuntimeException("Short link already exists"));

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isInternalServerError());
    }

    /**
     * Test case for the {@link LinkController#createLink(CreateLinkRequest)} method when
     * an error occurs during the short link generation process.
     */
    @Test
    @WithMockUser
    void createLinkInternalErrorTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.doesLinkExist(any())).thenReturn(true);

        CreateLinkRequest request = new CreateLinkRequest("https://www.example.com", null);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isInternalServerError());
    }

    /**
     * Test case for the {@link LinkController#deleteLink(String)} method.
     */
    @Test
    @WithMockUser
    void deleteLinkTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);
        doNothing().when(linkService).deleteByShortLink(link.getShortLink());

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", String.valueOf(link.getShortLink())));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"));
    }

    /**
     * Test case for the {@link LinkController#deleteLink(String)} method when
     * the authenticated user does not have rights.
     */
    @Test
    @WithMockUser
    void deleteLinkForbiddenTest() throws Exception {
        User newUser = User.builder()
                .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c83"))
                .email("test3@gmail.com")
                .password("Password3")
                .role(UserRole.USER)
                .build();
        when(userService.findByEmail(any())).thenReturn(newUser);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);
        doNothing().when(linkService).deleteByShortLink(link.getShortLink());

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", String.valueOf(link.getShortLink())));

        resultActions.andExpect(status().isForbidden());
    }

    /**
     * Test case for the {@link LinkController#editLinkContent(EditLinkContentRequest)} method.
     */
    @Test
    @WithMockUser
    void editLinkContentTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);

        EditLinkContentRequest request = new EditLinkContentRequest(link.getShortLink(), "shortLink2");
        when(linkService.update(link)).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/edit/content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"));
    }

    /**
     * Test case for the {@link LinkController#editLinkContent(EditLinkContentRequest)} method when
     * the authenticated user does not have rights.
     */
    @Test
    @WithMockUser
    void editLinkContentForbiddenTest() throws Exception {
        User newUser = User.builder()
                .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c83"))
                .email("test3@gmail.com")
                .password("Password3")
                .role(UserRole.USER)
                .build();
        when(userService.findByEmail(any())).thenReturn(newUser);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);

        EditLinkContentRequest request = new EditLinkContentRequest(link.getShortLink(), "shortLink2");
        when(linkService.update(link)).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/edit/content")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        resultActions.andExpect(status().isForbidden());
    }

    /**
     * Test case for the {@link LinkController#refreshLink(String)} method.
     */
    @Test
    @WithMockUser
    void refreshLinkTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);
        when(linkService.update(link)).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/edit/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", String.valueOf(link.getShortLink())));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"));
    }

    /**
     * Test case for the {@link LinkController#refreshLink(String)} method when
     * the authenticated user does not have rights.
     */
    @Test
    @WithMockUser
    void refreshLinkForbiddenTest() throws Exception {
        User newUser = User.builder()
                .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c83"))
                .email("test3@gmail.com")
                .password("Password3")
                .role(UserRole.USER)
                .build();
        when(userService.findByEmail(any())).thenReturn(newUser);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);
        when(linkService.update(link)).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(post("/api/V1/link/edit/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", String.valueOf(link.getShortLink())));

        resultActions.andExpect(status().isForbidden());
    }


    /**
     * Test case for the {@link LinkController#getInfoByShortLink(String)} method.
     */
    @Test
    @WithMockUser
    void getInfoByShortLinkTest() throws Exception {
        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(get("/api/V1/link/info")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", link.getShortLink()));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"))
                .andExpect(jsonPath("$.linkDtoList").isArray());
    }

    /**
     * Test case for the {@link LinkController#getInfoByShortLink(String)} method when
     * the authenticated user does not have rights.
     */
    @Test
    @WithMockUser
    void getInfoByShortLinkForbiddenTest() throws Exception {
        User newUser = User.builder()
                .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c83"))
                .email("test3@gmail.com")
                .password("Password3")
                .role(UserRole.USER)
                .build();
        when(userService.findByEmail(any())).thenReturn(newUser);
        when(linkService.findByShortLink(link.getShortLink())).thenReturn(link);

        ResultActions resultActions = mockMvc.perform(get("/api/V1/link/info")
                .contentType(MediaType.APPLICATION_JSON)
                .param("shortLink", link.getShortLink()));

        resultActions.andExpect(status().isForbidden());
    }

    /**
     * Test case for the {@link LinkController#getAllLinksForUser()} method.
     */
    @Test
    @WithMockUser
    void getAllLinksForUserTest() throws Exception {
        List<Link> userLinks = Arrays.asList(
                Link.builder().id(UUID.randomUUID()).user(user).build(),
                Link.builder().id(UUID.randomUUID()).user(user).build(),
                Link.builder().id(UUID.randomUUID()).user(user).build());

        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.findAllByUserId(user.getId())).thenReturn(userLinks);

        ResultActions resultActions = mockMvc.perform(get("/api/V1/link/all-links-info")
                .contentType(MediaType.APPLICATION_JSON)
                .param("userId", String.valueOf(user.getId())));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value("ok"))
                .andExpect(jsonPath("$.linkDtoList").isArray())
                .andExpect(jsonPath("$.linkDtoList.length()").value(userLinks.size()));
    }

    /**
     * Test case for the {@link LinkController#getLinksStatsForUser()} method.
     */
    @Test
    @WithMockUser
    void getLinksStatsForUserTest() throws Exception {
        List<LinkStatisticsDto> stats = Arrays.asList(
                new LinkStatisticsDto(UUID.randomUUID(), "link1", 10),
                new LinkStatisticsDto(UUID.randomUUID(), "link2", 20)
        );

        when(userService.findByEmail(any())).thenReturn(user);
        when(linkService.getLinkUsageStatsByUserId(user.getId())).thenReturn(stats);
        ResultActions resultActions = mockMvc.perform(get("/api/V1/link/url-usage-top-for-user")
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.linksStatsList").isArray())
                .andExpect(jsonPath("$.error").value("ok"));
    }
}

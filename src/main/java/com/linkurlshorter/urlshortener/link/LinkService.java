package com.linkurlshorter.urlshortener.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class for managing link entities.
 */
@Service
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;

    /**
     * Saves a link entity.
     *
     * @param link The link entity to save.
     * @return The saved link entity.
     * @throws NullPropertyException If the 'link' parameter is null.
     */
    public Link save(Link link) {
        try{
            return linkRepository.save(link);
        } catch(Exception e){
            throw new NullPropertyException();
        }
    }

    /**
     * Updates a link entity.
     *
     * @param link The link entity to update.
     * @return The updated link entity.
     * @throws NullPropertyException If the 'link' parameter is null.
     * @throws DeletedLinkException If the link has been marked as deleted.
     */
    public Link update(Link link) {
        throwIfDeleted(link);
        try{
            return linkRepository.save(link);
        } catch(Exception e){
            throw new NullPropertyException();
        }
    }

    /**
     * Retrieves a link entity by its ID.
     *
     * @param id The ID of the link entity to retrieve.
     * @return The retrieved link entity.
     * @throws NullPropertyException If the 'id' parameter is null.
     * @throws NoLinkFoundByIdException If no link is found with the given ID.
     * @throws DeletedLinkException If the retrieved link has been marked as deleted.
     */
    public Link findById(UUID id) {
        if(Objects.isNull(id)){
            throw new NullPropertyException();
        }
        Link link = linkRepository.findById(id).orElseThrow(NoLinkFoundByIdException::new);
        if(link.getStatus()==LinkStatus.DELETED){
            throw new DeletedLinkException();
        }
        return link;
    }

    /**
     * Retrieves a link entity by its short link.
     *
     * @param shortLink The short link of the link entity to retrieve.
     * @return The retrieved link entity.
     * @throws NullPropertyException If the 'shortLink' parameter is null.
     * @throws NoLinkFoundByShortLinkException If no link is found with the given short link.
     * @throws DeletedLinkException If the retrieved link has been marked as deleted.
     */
    public Link findByShortLink(String shortLink) {
        if(Objects.isNull(shortLink)){
            throw new NullPropertyException();
        }
        Link link = linkRepository.findByShortLink(shortLink).orElseThrow(NoLinkFoundByShortLinkException::new);
        if(link.getStatus()==LinkStatus.DELETED){
            throw new DeletedLinkException();
        }
        return link;
    }

    /**
     * Marks a link entity as deleted by its short link.
     *
     * @param shortLink The short link of the link entity to mark as deleted.
     * @throws NullPropertyException If the 'shortLink' parameter is null.
     * @throws NoLinkFoundByShortLinkException If no link is found with the given short link.
     * @throws DeletedLinkException If the link has already been marked as deleted.
     */
    public void deleteByShortLink(String shortLink) {
        if(Objects.isNull(shortLink)){
            throw new NullPropertyException();
        }
        Link link = findByShortLink(shortLink);
        link.setStatus(LinkStatus.DELETED);
        update(link);
    }

    /**
     * Throws a DeletedLinkException if the link has been marked as deleted.
     *
     * @param link The link entity to check.
     * @throws DeletedLinkException If the link has been marked as deleted.
     */
    private void throwIfDeleted(Link link) {
        if (findByShortLink(link.getShortLink()).getStatus() == LinkStatus.DELETED) {
            throw new DeletedLinkException();
        }
    }
}

package com.linkurlshorter.urlshortener.link;

import com.linkurlshorter.urlshortener.link.dto.LinkStatisticsDto;
import com.linkurlshorter.urlshortener.link.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing links in the database.
 *
 * <p>Extends {@link JpaRepository} to inherit common CRUD operations for link entities. Created on: 13.04.2024
 *
 * @author Artem Poliakov
 * @version 1.0
 * @see Link
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, UUID> {

    /**
     * Retrieves a link entity by its short link.
     *
     * @param shortLink The short link of the link entity to retrieve.
     * @return An {@link java.util.Optional} containing the retrieved link entity, or empty if no link is found with the specified short link.
     */
    Optional<Link> findByShortLink(String shortLink);

    /**
     * Retrieves a list of links associated with the specified user ID, excluding those with a status of 'DELETED'.
     * This method executes a JPQL query to fetch all links associated with the given user ID,
     * excluding those with a status set to 'DELETED'.
     *
     * @param userId The ID of the user whose links are to be retrieved.
     * @return A list of Link objects associated with the specified user ID, excluding links with a status of 'DELETED'.
     */
    @Query("SELECT l from Link l WHERE l.user.id = :userId")
    List<Link> findAllByUserId(@Param(value = "userId") UUID userId);

    /**
     * Retrieves a list of active links associated with the specified user ID.
     * <p>
     * This method executes a JPQL query to fetch all active links associated with the given user ID.
     * Active links are those whose status is set to 'ACTIVE'.
     *
     * @param userId The ID of the user whose active links are to be retrieved.
     * @return A list of active Link objects associated with the specified user ID.
     */
    @Query("SELECT l FROM Link l WHERE l.user.id = :userId AND l.status = 'ACTIVE'")
    List<Link> findAllActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT l FROM Link l WHERE l.status = 'ACTIVE'")
    List<Link> findAllActive();

    /**
     * Retrieves a list of link usage statistics for a specific user.
     *
     * <p>This method executes a JPQL query to fetch the link usage statistics for the user with the given ID.
     * It retrieves statistics for all links associated with the specified user ID, excluding links with a status set to 'DELETED'.
     * The method constructs and returns a list of LinkStatisticsDto objects containing link ID, short link, and usage statistics.
     *
     * @param userId The ID of the user whose link usage statistics are to be retrieved.
     * @return A list of LinkStatisticsDto objects containing link usage statistics for the specified user.
     */
    @Query("SELECT new com.linkurlshorter.urlshortener.link.dto.LinkStatisticsDto(l.id, l.shortLink, l.statistics)" +
            " FROM Link l WHERE l.user.id = :userId")
    List<LinkStatisticsDto> getLinkUsageStatsForUser(@Param(value = "userId") UUID userId);

    /**
     * Deletes a link entity by its ID.
     *
     * @param id The ID of the link entity to delete.
     */
    void deleteById(UUID id);
}

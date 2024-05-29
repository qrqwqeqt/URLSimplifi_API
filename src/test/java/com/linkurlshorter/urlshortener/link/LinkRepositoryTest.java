package com.linkurlshorter.urlshortener.link;

import com.linkurlshorter.urlshortener.link.model.Link;
import com.linkurlshorter.urlshortener.link.model.LinkStatus;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link LinkRepository} class.
 *
 * @author Ivan Shalaiev
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LinkRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16.0-alpine");
    @Autowired
    LinkRepository linkRepository;
    private Link link;

    @BeforeEach
    void setUp() {
        link = Link.builder()
                .id(UUID.fromString("3053e49b-6da3-4389-9d06-23b2d57b6f25"))
                .longLink("https://www.youtube.com")
                .shortLink("short-link-1")
                .user(User.builder()
                        .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c81"))
                        .email("user1@example.com")
                        .password("$2a$12$7Cp4On1DBNyCkz4TaZYc3O.A.CBKi4WXgXnlI4SD0yn7CgBX5Gd6O")
                        .role(UserRole.USER)
                        .build())
                .createdTime(LocalDateTime.of(2024, 4, 13, 10, 0))
                .expirationTime(LocalDateTime.of(2025, 5, 16, 8, 0))
                .statistics(100)
                .status(LinkStatus.ACTIVE)
                .build();
    }

    /**
     * Test to verify that the database container is created and running.
     */
    @Test
    void connectionEstablished() {
        assertThat(container.isCreated()).isTrue();
        assertThat(container.isRunning()).isTrue();
    }

    /**
     * Test to verify the {@link LinkRepository#deleteAll()} method.
     * It ensures that all links are deleted from the database and the repository is empty.
     */
    @Test
    void thatDeleteAllWorksCorrectly() {
        linkRepository.deleteAll();
        assertThat(linkRepository.findAll()).isEmpty();
    }

    /**
     * Test to verify the {@link LinkRepository#findByShortLink(String)} method.
     * It ensures that the findByShortLink method returns the correct link entity when provided with a valid short link.
     */
    @Test
    void thatFindByShortLinkWorksCorrectly() {
        Link found = linkRepository.findByShortLink(link.getShortLink()).orElseThrow();
        assertThat(found)
                .isNotNull()
                .isEqualTo(link);
    }

    /**
     * Test to verify the {@link LinkRepository#findAll()} method.
     * It ensures that the findAll method returns a non-empty list of links from the database.
     */
    @Test
    void thatFindAllWorksCorrectly() {
        List<Link> links = linkRepository.findAll();
        assertThat(links).isNotNull()
                .hasSize(5);
    }

    /**
     * Test to verify the {@link LinkRepository#deleteById(UUID)} method.
     * It ensures that the deleteById method removes the link with the specified ID from the database.
     */
    @Test
    void thatDeleteByIdWorksCorrectly() {
        linkRepository.deleteById(link.getId());
        assertThat(linkRepository.findById(link.getId())).isEmpty();
    }
}
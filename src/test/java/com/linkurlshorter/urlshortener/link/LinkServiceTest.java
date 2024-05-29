package com.linkurlshorter.urlshortener.link;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkurlshorter.urlshortener.link.dto.LinkStatisticsDto;
import com.linkurlshorter.urlshortener.link.exception.InactiveLinkException;
import com.linkurlshorter.urlshortener.link.exception.NoLinkFoundByShortLinkException;
import com.linkurlshorter.urlshortener.link.exception.NullLinkPropertyException;
import com.linkurlshorter.urlshortener.link.model.Link;
import com.linkurlshorter.urlshortener.link.model.LinkStatus;
import com.linkurlshorter.urlshortener.user.model.User;
import com.linkurlshorter.urlshortener.user.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LinkService} class.
 *
 * @author Anastasiia Usenko
 */
@ExtendWith(MockitoExtension.class)
class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkService linkService;

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    @Mock
    private ObjectMapper mapper;

    private Link link;

    /**
     * Set up method to initialize test data before each test method.
     */
    @BeforeEach
    void setUp() {
        link = Link.builder()
                .id(UUID.fromString("3053e49b-6da3-4389-9d06-23b2d57b6f25"))
                .longLink("https://www.youtube.com")
                .shortLink("short-link-1")
                .user(User.builder()
                        .id(UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c81"))
                        .email("user1@example.com")
                        .password("password1")
                        .role(UserRole.USER)
                        .build())
                .createdTime(LocalDateTime.of(2024, 4, 13, 10, 0))
                .expirationTime(LocalDateTime.of(2025, 5, 16, 8, 0))
                .statistics(100)
                .status(LinkStatus.ACTIVE)
                .build();
    }

    /**
     * Test case for the {@link LinkService#save(Link)} method.
     */
    @Test
    void saveSuccessfulTest() {
        when(linkRepository.save(any(Link.class))).thenReturn(link);
        Link savedLink = linkService.save(link);

        assertThat(savedLink).isNotNull().isEqualTo(link);
        verify(linkRepository, times(1)).save(link);
    }

    /**
     * Test case for the {@link LinkService#save(Link)} method when the provided link is null.
     */
    @Test
    void saveFailedTest() {
        assertThatThrownBy(() -> linkService.save(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }

    /**
     * Test case for the {@link LinkService#update(Link)} method.
     */
    @Test
    void updateSuccessfulTest() {
        when(linkRepository.save(link)).thenReturn(link);

        Link savedLink = linkService.update(link);
        assertThat(savedLink).isNotNull().isEqualTo(link);
    }

    /**
     * Test case for the {@link LinkService#update(Link)} method when the provided link is null.
     */
    @Test
    void updateNullLinkTest() {
        assertThatThrownBy(() -> linkService.update(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }


    /**
     * Test case for the {@link LinkService#updateRedisShortLink(String, String)} method.
     */
    @Test
    void updateRedisShortLinkTest() {
        String shortLink = link.getShortLink();
        String newShortLink = "short-link-2";

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(anyString())).thenReturn(true);
        linkService.updateRedisShortLink(shortLink, newShortLink);

        verify(jedis, times(1)).rename(shortLink, newShortLink);
    }

    /**
     * Test case for the {@link LinkService#updateRedisLink(String, Link)} method.
     */
    @Test
    void updateRedisLinkTest() throws JsonProcessingException {
        String shortLink = link.getShortLink();

        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(anyString())).thenReturn(true);
        linkService.updateRedisLink(shortLink, link);

        verify(jedis, times(1)).set(shortLink, mapper.writeValueAsString(link));
    }

    /**
     * Test case for the {@link LinkService#getLongLinkFromShortLink(String)} method.
     */
    @Test
    void getLongLinkFromShortLinkTest() throws JsonProcessingException {
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(anyString())).thenReturn(true);
        when(jedis.get(anyString())).thenReturn("{}");
        when(mapper.readValue(anyString(), eq(Link.class))).thenReturn(link);

        String actualLongLink = linkService.getLongLinkFromShortLink(link.getShortLink());

        assertThat(actualLongLink).isEqualTo(link.getLongLink());
    }

    /**
     * Test case for the {@link LinkService#getLongLinkFromShortLink(String)} method when link status
     * is inactive.
     */
    @Test
    void getLongLinkFromShortLinkInactiveTest() throws JsonProcessingException {
        link.setStatus(LinkStatus.INACTIVE);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(anyString())).thenReturn(true);
        when(jedis.get(anyString())).thenReturn("{}");
        when(mapper.readValue(anyString(), eq(Link.class))).thenReturn(link);

        assertThatThrownBy(() -> linkService.getLongLinkFromShortLink(link.getShortLink()))
                .isInstanceOf(InactiveLinkException.class);
    }

    /**
     * Test case for the {@link LinkService#getLongLinkFromShortLink(String)} method when link expiration time
     * has passed.
     */
    @Test
    void getLongLinkFromShortLinkExpiredTest() throws JsonProcessingException {
        link.setExpirationTime(LocalDateTime.now().minusDays(1));
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.exists(anyString())).thenReturn(true);
        when(jedis.get(anyString())).thenReturn("{}");
        when(mapper.readValue(anyString(), eq(Link.class))).thenReturn(link);

        assertThatThrownBy(() -> linkService.getLongLinkFromShortLink(link.getShortLink()))
                .isInstanceOf(InactiveLinkException.class);
    }

    /**
     * Test case for the {@link LinkService#findByShortLink(String)} method.
     */
    @Test
    void findByShortLinkTest() {
        when(linkRepository.findByShortLink(link.getShortLink())).thenReturn(Optional.of(link));
        Link foundLink = linkService.findByShortLink(link.getShortLink());

        assertThat(foundLink).isNotNull().isEqualTo(link);
        verify(linkRepository, times(1)).findByShortLink(link.getShortLink());
    }

    /**
     * Test case for the {@link LinkService#findByShortLink(String)} method when the
     * provided short link is null.
     */
    @Test
    void findByNullShortLinkTest() {
        assertThatThrownBy(() -> linkService.findByShortLink(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }

    /**
     * Test case for the {@link LinkService#findByShortLink(String)} method when the
     * provided short link does not exist.
     */
    @Test
    void findByShortLinkNotFoundTest() {
        assertThatThrownBy(() -> linkService.findByShortLink("short-link-2"))
                .isInstanceOf(NoLinkFoundByShortLinkException.class);
    }



    /**
     * Test case for the {@link LinkService#findAllByUserId(UUID)} method.
     */
    @Test
    void findByAllByUserIdTest() {
        UUID userId = UUID.fromString("84991c79-f6a9-4b7b-b1b4-0d66c0b92c81");
        User user = User.builder().id(userId).build();
        LocalDateTime expirationTime = LocalDateTime.now().plusDays(1);
        List<Link> userLinks = Arrays.asList(
                Link.builder().id(UUID.randomUUID()).user(user).expirationTime(expirationTime).build(),
                Link.builder().id(UUID.randomUUID()).user(user).expirationTime(expirationTime).build(),
                Link.builder().id(UUID.randomUUID()).user(user).expirationTime(expirationTime).build()
        );

        when(linkRepository.findAllByUserId(userId)).thenReturn(userLinks);
        List<Link> foundLinks = linkService.findAllByUserId(userId);

        assertThat(foundLinks).isNotNull().isEqualTo(userLinks);
        verify(linkRepository, times(1)).findAllByUserId(userId);
    }

    /**
     * Test case for the {@link LinkService#findAllByUserId(UUID)} method when the provided user is null.
     */
    @Test
    void findAllByNullUserTest() {
        assertThatThrownBy(() -> linkService.findAllByUserId(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }

    /**
     * Test case for the {@link LinkService#getLinkUsageStatsByUserId(UUID)} method.
     */
    @Test
    void getLinkUsageStatsByUserIdTest() {
        UUID userId = UUID.randomUUID();
        List<LinkStatisticsDto> expectedStats = Collections.singletonList(
                new LinkStatisticsDto(UUID.randomUUID(), "shortLink1", 10)
        );
        when(linkRepository.getLinkUsageStatsForUser(userId)).thenReturn(expectedStats);

        List<LinkStatisticsDto> actualStats = linkService.getLinkUsageStatsByUserId(userId);

        assertThat(actualStats).isNotNull().isEqualTo(expectedStats);
        verify(linkRepository, times(1)).getLinkUsageStatsForUser(userId);
    }

    /**
     * Test case for the {@link LinkService#getLinkUsageStatsByUserId(UUID)} method when the provided id is null.
     */
    @Test
    void getLinkUsageStatsByUserIdNullTest() {
        assertThatThrownBy(() -> linkService.getLinkUsageStatsByUserId(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }

    /**
     * Test case for the {@link LinkService#deleteByShortLink(String)} method.
     */
    @Test
    void deleteByShortLinkTest() {
        when(jedisPool.getResource()).thenReturn(jedis);
        when(linkRepository.findByShortLink(link.getShortLink())).thenReturn(Optional.of(link));
        linkService.deleteByShortLink(link.getShortLink());
        verify(linkRepository, times(1)).findByShortLink(link.getShortLink());
        verify(linkRepository, times(1)).delete(link);
    }

    /**
     * Test case for the {@link LinkService#deleteByShortLink(String)} method when the
     * provided short link is null.
     */
    @Test
    void deleteByNullShortLinkTest() {
        assertThatThrownBy(() -> linkService.deleteByShortLink(null))
                .isInstanceOf(NullLinkPropertyException.class);
    }
}

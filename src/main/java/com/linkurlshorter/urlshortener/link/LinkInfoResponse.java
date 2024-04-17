package com.linkurlshorter.urlshortener.link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LinkInfoResponse {
    private List<LinkInfoDto> linkDtoList;
    private String error;
}

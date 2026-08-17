package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The reference to a freshly stored image. Only the URL is ever carried around afterwards — cart
 * and order records hold this reference, never a second copy of the bytes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UploadedImageResponse", description = "A stored image reference")
public class UploadedImageResponse {

    @Schema(example = "3f2a91c4d8e14b7a9c0f5e6d7b8a1c2d")
    private String id;

    @Schema(example = "/api/v1/images/3f2a91c4d8e14b7a9c0f5e6d7b8a1c2d")
    private String url;
}

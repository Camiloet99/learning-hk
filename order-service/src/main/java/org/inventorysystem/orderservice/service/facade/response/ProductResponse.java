package org.inventorysystem.orderservice.service.facade.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductResponse {

    private Long id;

    private String name;

    private Long categoryId;

    private Integer quantity;

    private String description;

}

package com.Invnmgmt.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDTO {
  
	 private Long id;

	    @NotBlank(message = "Name is required")
	    private String name;

	    private List<ProductDTO> products;

	    public CategoryDTO() {
	    }

	    public CategoryDTO(Long id, String name, List<ProductDTO> products) {
	        this.id = id;
	        this.name = name;
	        this.products = products;
	    }

	    public Long getId() {
	        return id;
	    }

	    public void setId(Long id) {
	        this.id = id;
	    }

	    public String getName() {
	        return name;
	    }

	    public void setName(String name) {
	        this.name = name;
	    }

	    public List<ProductDTO> getProducts() {
	        return products;
	    }

	    public void setProducts(List<ProductDTO> products) {
	        this.products = products;
	    }
	
}

package com.Invnmgmt.services;

import com.Invnmgmt.dtos.CategoryDTO;
import com.Invnmgmt.dtos.Response;

public interface CategoryService {

	Response createCategory(CategoryDTO categoryDTO);

    Response getAllCategories();

    Response getCategoryById(Long id);

    Response updateCategory(Long id, CategoryDTO categoryDTO);

    Response deleteCategory(Long id);
}

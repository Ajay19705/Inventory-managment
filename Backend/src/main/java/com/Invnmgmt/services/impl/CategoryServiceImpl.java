package com.Invnmgmt.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.Invnmgmt.dtos.CategoryDTO;
import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.Category;
import com.Invnmgmt.repositories.CategoryRepository;
import com.Invnmgmt.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService{

	
	private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

 
    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Response createCategory(CategoryDTO categoryDTO) {
        Category categoryToSave = modelMapper.map(categoryDTO, Category.class);
        categoryRepository.save(categoryToSave);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Category Saved Successfully");
        return response;
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        categories.forEach(category -> category.setProducts(null));

        List<CategoryDTO> categoryDTOList = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>() {}.getType());

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setCategories(categoryDTOList);
        return response;
    }

    @Override
    public Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setCategory(categoryDTO);
        return response;
    }

    @Override
    public Response updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Category Was Successfully Updated");
        return response;
    }

    @Override
    public Response deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        categoryRepository.deleteById(id);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Category Was Successfully Deleted");
        return response;
    }
}

package com.Invnmgmt.services.impl;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Invnmgmt.dtos.ProductDTO;
import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.Category;
import com.Invnmgmt.models.Product;
import com.Invnmgmt.repositories.CategoryRepository;
import com.Invnmgmt.repositories.ProductRepository;
import com.Invnmgmt.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService {
	
	 private final ProductRepository productRepository;
	    private final ModelMapper modelMapper;
	    private final CategoryRepository categoryRepository;
	    private static final Logger log = Logger.getLogger(ProductServiceImpl.class.getName());


	    private static final String IMAGE_DIRECTORY_2 = "D:/MyProject/New folder (2)/invnmgmtreact/public/products";

	    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper, CategoryRepository categoryRepository) {
	        this.productRepository = productRepository;
	        this.modelMapper = modelMapper;
	        this.categoryRepository = categoryRepository;
	    }

	    private String generateUniqueSku() {
	        String sku;
	        do {
	            sku = String.valueOf((long)(Math.random() * 9000000000L) + 1000000000L); // Generates a 10-digit number
	        } while (productRepository.existsBySku(sku));
	        return sku;
	    }
	    
	    @Override
	    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {

	        Category category = categoryRepository.findById(productDTO.getCategoryId())
	                .orElseThrow(() -> new NotFoundException("Category Not Found"));

	        Product productToSave = new Product();
	        productToSave.setName(productDTO.getName());
//	        productToSave.setSku(productDTO.getSku());
	        if (productDTO.getSku() == null || productDTO.getSku().isBlank()) {
	            productToSave.setSku(generateUniqueSku());
	        } else {
	            productToSave.setSku(productDTO.getSku());
	        }
	        productToSave.setPrice(productDTO.getPrice());
	        productToSave.setStockQuantity(productDTO.getStockQuantity());
	        productToSave.setDescription(productDTO.getDescription());
	        productToSave.setCategory(category);

	        if (imageFile != null && !imageFile.isEmpty()) {
	            log.info("Image file exists");
	            String imagePath = saveImage2(imageFile);
	            System.out.println("IMAGE URL IS: " + imagePath);
	            productToSave.setImageUrl(imagePath);
	        }

	        productRepository.save(productToSave);

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("Product successfully saved");
	        response.setProduct(modelMapper.map(productToSave, ProductDTO.class));
	        return response;
	    }

	    @Override
	    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {

	        Product existingProduct = productRepository.findById(productDTO.getProductId())
	                .orElseThrow(() -> new NotFoundException("Product Not Found"));

	        if (imageFile != null && !imageFile.isEmpty()) {
	            String imagePath = saveImage2(imageFile);
	            System.out.println("IMAGE URL IS: " + imagePath);
	            existingProduct.setImageUrl(imagePath);
	        }

	        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0) {
	            Category category = categoryRepository.findById(productDTO.getCategoryId())
	                    .orElseThrow(() -> new NotFoundException("Category Not Found"));
	            existingProduct.setCategory(category);
	        }

	        if (productDTO.getName() != null && !productDTO.getName().isBlank()) {
	            existingProduct.setName(productDTO.getName());
	        }

	        if (productDTO.getSku() != null && !productDTO.getSku().isBlank()) {
	            existingProduct.setSku(productDTO.getSku());
	        }

	        if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
	            existingProduct.setDescription(productDTO.getDescription());
	        }

	        if (productDTO.getPrice() != null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
	            existingProduct.setPrice(productDTO.getPrice());
	        }

	        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() >= 0) {
	            existingProduct.setStockQuantity(productDTO.getStockQuantity());
	        }

	        productRepository.save(existingProduct);

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("Product Updated successfully");
	        return response;
	    }

	    @Override
	    public Response getAllProducts() {

	        List<Product> productList = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
	        List<ProductDTO> productDTOList = modelMapper.map(productList, new TypeToken<List<ProductDTO>>() {}.getType());

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("success");
	        response.setProducts(productDTOList);
	        return response;
	    }

	    @Override
	    public Response getProductById(Long id) {
	        Product product = productRepository.findById(id)
	                .orElseThrow(() -> new NotFoundException("Product Not Found"));

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("success");
	        response.setProduct(modelMapper.map(product, ProductDTO.class));
	        return response;
	    }

	    @Override
	    public Response deleteProduct(Long id) {
	        productRepository.findById(id)
	                .orElseThrow(() -> new NotFoundException("Product Not Found"));

	        productRepository.deleteById(id);

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("Product Deleted successfully");
	        return response;
	    }

	    @Override
	    public Response searchProduct(String input) {

	        List<Product> products = productRepository.findByNameContainingOrDescriptionContaining(input, input);

	        if (products.isEmpty()) {
	            throw new NotFoundException("Product Not Found");
	        }

	        List<ProductDTO> productDTOList = modelMapper.map(products, new TypeToken<List<ProductDTO>>() {}.getType());

	        Response response = new Response();
	        response.setStatus(200);
	        response.setMessage("success");
	        response.setProducts(productDTOList);
	        return response;
	    }



	    private String saveImage2(MultipartFile imageFile) {
	        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 1024L * 1024 * 1024) {
	            throw new IllegalArgumentException("Only image files under 1GB are allowed");
	        }

	        File directory = new File(IMAGE_DIRECTORY_2);
	        if (!directory.exists()) {
	            directory.mkdirs();
	            log.info("Directory was created: " + directory.getAbsolutePath());
	        }

	        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
	        String imagePath = Paths.get(IMAGE_DIRECTORY_2, uniqueFileName).toString();
	        System.out.println("Saving file to: " + imagePath);

	        try {
	            File destinationFile = new File(imagePath);
	            imageFile.transferTo(destinationFile);
	        } catch (Exception e) {
	            throw new IllegalArgumentException("Error saving Image: " + e.getMessage());
	        }

	        return "http://localhost:8080/products/" + uniqueFileName;
	    }
}

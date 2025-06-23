package com.Invnmgmt.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.TransactionDTO;
import com.Invnmgmt.dtos.TransactionRequest;
import com.Invnmgmt.enums.TransactionStatus;
import com.Invnmgmt.enums.TransactionType;
import com.Invnmgmt.exceptions.NameValueRequiredException;
import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.Product;
import com.Invnmgmt.models.Supplier;
import com.Invnmgmt.models.Transaction;
import com.Invnmgmt.models.User;
import com.Invnmgmt.repositories.ProductRepository;
import com.Invnmgmt.repositories.SupplierRepository;
import com.Invnmgmt.repositories.TransactionRepository;
import com.Invnmgmt.services.TransactionService;
import com.Invnmgmt.services.UserService;
import com.Invnmgmt.specification.TransactionFilter;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private static final Logger logger = Logger.getLogger(TransactionServiceImpl.class.getName());

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  ProductRepository productRepository,
                                  SupplierRepository supplierRepository,
                                  UserService userService,
                                  ModelMapper modelMapper) {
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Override
    public Response purchase(TransactionRequest transactionRequest) {
        try {
            Long productId = transactionRequest.getProductId();
            Long supplierId = transactionRequest.getSupplierId();
            Integer quantity = transactionRequest.getQuantity();

            if (supplierId == null) throw new NameValueRequiredException("Supplier Id is required.");
            if (productId == null) throw new NameValueRequiredException("Product Id is required.");
            if (quantity == null || quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than 0.");

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found."));

            if (product.getPrice() == null) throw new IllegalArgumentException("Product price is not set.");
            if (product.getStockQuantity() == null) product.setStockQuantity(0);

            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new NotFoundException("Supplier not found."));

            User user = userService.getCurrentLoggedInUser();
            if (user == null) throw new IllegalArgumentException("Logged-in user not found.");

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);

            // Create and save transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionType(TransactionType.PURCHASE);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProduct(product);
            transaction.setUser(user); // no cascade!
            transaction.setSupplier(supplier);
            transaction.setTotalProducts(quantity);
            transaction.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            transaction.setDescription(transactionRequest.getDescription());
            transaction.setNote(transactionRequest.getNote());

            transactionRepository.save(transaction);

            Response response = new Response();
            response.setStatus(200);
            response.setMessage("Purchase made successfully");
            return response;

        } catch (Exception e) {
            e.printStackTrace(); // Log error clearly
            throw e; // Optional: wrap in custom exception
        }
    }
    
    
    @Override
    public Response sell(TransactionRequest transactionRequest) {
        Long productId = transactionRequest.getProductId();
        Integer quantity = transactionRequest.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.SALE);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setProduct(product);
        transaction.setUser(user);
        transaction.setTotalProducts(quantity);
        transaction.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        transaction.setDescription(transactionRequest.getDescription());
        transaction.setNote(transactionRequest.getNote());

        transactionRepository.save(transaction);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Product Sold successfully ");
        return response;
    }

    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {
        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id is Required");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        User user = userService.getCurrentLoggedInUser();

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.RETURN_TO_SUPPLIER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setProduct(product);
        transaction.setUser(user);
        transaction.setSupplier(supplier);
        transaction.setTotalProducts(quantity);
        transaction.setTotalPrice(BigDecimal.ZERO);
        transaction.setDescription(transactionRequest.getDescription());
        transaction.setNote(transactionRequest.getNote());

        transactionRepository.save(transaction);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Product Returned in progress");
        return response;
    }

    @Override
    public Response getAllTransactions(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<Transaction> spec = TransactionFilter.byFilter(filter);


        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransactionDTO> transactionDTOS = modelMapper.map(
                transactionPage.getContent(),
                new TypeToken<List<TransactionDTO>>() {}.getType()
        );

        for (TransactionDTO dto : transactionDTOS) {
            dto.setUser(null);
//            dto.setProduct(null);
            dto.setSupplier(null);
        }

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setTransactions(transactionDTOS);
        response.setTotalElements(transactionPage.getTotalElements());
        response.setTotalPages(transactionPage.getTotalPages());
        return response;
    }

    @Override
    public Response getAllTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);
        transactionDTO.getUser().setTransactions(null);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setTransaction(transactionDTO);
        return response;
    }

    @Override
    public Response getAllTransactionByMonthAndYear(int month, int year) {

        List<Transaction> transactions = transactionRepository.findAll(TransactionFilter.byMonthAndYear(month, year));

        List<TransactionDTO> transactionDTOS = modelMapper.map(transactions,
                new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOS.forEach(transactionDTO -> {
            transactionDTO.setUser(null);
            transactionDTO.setProduct(null);
            transactionDTO.setSupplier(null);
        });

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setTransactions(transactionDTOS);
        return response;
    }

    @Override
    public Response updateTransactionStatus(Long transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));

        transaction.setStatus(status);
        transaction.setUpdateAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Transaction Status Successfully Updated");
        return response;
    }
}

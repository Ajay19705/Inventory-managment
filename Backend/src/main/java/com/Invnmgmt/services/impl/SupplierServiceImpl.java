package com.Invnmgmt.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.Invnmgmt.dtos.Response;
import com.Invnmgmt.dtos.SupplierDTO;
import com.Invnmgmt.exceptions.NotFoundException;
import com.Invnmgmt.models.Supplier;
import com.Invnmgmt.repositories.SupplierRepository;
import com.Invnmgmt.services.SupplierService;

@Service
public class SupplierServiceImpl implements SupplierService {

	private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    public SupplierServiceImpl(SupplierRepository supplierRepository, ModelMapper modelMapper) {
        this.supplierRepository = supplierRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Response addSupplier(SupplierDTO supplierDTO) {
        Supplier supplierToSave = modelMapper.map(supplierDTO, Supplier.class);
        supplierRepository.save(supplierToSave);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Supplier Saved Successfully");
        return response;
    }

    @Override
    public Response updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        if (supplierDTO.getName() != null) existingSupplier.setName(supplierDTO.getName());
        if (supplierDTO.getContactInfo() != null) existingSupplier.setContactInfo(supplierDTO.getContactInfo());
        if (supplierDTO.getAddress() != null) existingSupplier.setAddress(supplierDTO.getAddress());

        supplierRepository.save(existingSupplier);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Supplier Was Successfully Updated");
        return response;
    }

    @Override
    public Response getAllSupplier() {
        List<Supplier> suppliers = supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<SupplierDTO> supplierDTOList = modelMapper.map(suppliers, new TypeToken<List<SupplierDTO>>() {}.getType());

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setSuppliers(supplierDTOList);
        return response;
    }

    @Override
    public Response getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        SupplierDTO supplierDTO = modelMapper.map(supplier, SupplierDTO.class);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("success");
        response.setSupplier(supplierDTO);
        return response;
    }

    @Override
    public Response deleteSupplier(Long id) {
        supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        supplierRepository.deleteById(id);

        Response response = new Response();
        response.setStatus(200);
        response.setMessage("Supplier Was Successfully Deleted");
        return response;
    }
}

package com.ecommerce.service;

import com.ecommerce.entity.Customer;
import com.ecommerce.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Integer id) {
        log.info("Fetching customer with ID: {}", id);
        return customerRepository.findById(id).orElse(null);
    }

    public Customer createCustomer(Customer customer) {
        log.info("Creating customer: {}", customer.getEmail());
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Integer id, Customer customer) {
        log.info("Updating customer with ID: {}", id);
        if (customerRepository.existsById(id)) {
            customer.setId(id);
            return customerRepository.save(customer);
        }
        return null;
    }

    public void deleteCustomer(Integer id) {
        log.info("Deleting customer with ID: {}", id);
        customerRepository.deleteById(id);
    }

    public List<Customer> getCustomersByCity(String city) {
        log.info("Fetching customers from city: {}", city);
        return customerRepository.findByCityContainingIgnoreCase(city);
    }
}
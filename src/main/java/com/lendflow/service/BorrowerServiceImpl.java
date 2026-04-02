package com.lendflow.service;

import com.lendflow.entity.Borrower;
import com.lendflow.exception.ResourceNotFoundException;
import com.lendflow.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerServiceImpl implements IBorrowerService {

    private final BorrowerRepository borrowerRepository;

    @Override
    public List<Borrower> getAllBorrowers() {
        return borrowerRepository.findAllByOrderByDateAddedDesc();
    }

    @Override
    public Borrower getBorrowerById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower", "ID", id));
    }

    @Override
    public Borrower saveBorrower(Borrower borrower) {
        return borrowerRepository.save(borrower);
    }

    @Override
    public void deleteBorrower(Long id) {
        borrowerRepository.deleteById(id);
    }
}


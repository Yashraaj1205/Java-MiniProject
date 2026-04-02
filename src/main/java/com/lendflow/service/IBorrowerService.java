package com.lendflow.service;

import com.lendflow.entity.Borrower;
import java.util.List;

public interface IBorrowerService {
    List<Borrower> getAllBorrowers();
    Borrower getBorrowerById(Long id);
    Borrower saveBorrower(Borrower borrower);
}


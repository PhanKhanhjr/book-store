package phankhanh.book_store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import phankhanh.book_store.domain.Supplier;
import phankhanh.book_store.repository.SupplierRepository;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierRepository repo;

    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody Supplier req) {
        return ResponseEntity.ok(repo.save(req));
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> list() {
        return ResponseEntity.ok(repo.findAll());
    }
}


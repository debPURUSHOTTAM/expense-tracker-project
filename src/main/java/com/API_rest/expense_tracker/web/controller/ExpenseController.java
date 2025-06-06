package com.API_rest.expense_tracker.web.controller;

import com.API_rest.expense_tracker.persistence.entities.ExpenseEntity;
import com.API_rest.expense_tracker.persistence.entities.UserEntity;
import com.API_rest.expense_tracker.service.ExpenseService;
import com.API_rest.expense_tracker.service.UserService;
import com.API_rest.expense_tracker.web.dto.ExpenseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated.");
        }

        String username = authentication.getName();
        UserEntity user = userService.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return user;
    }

    @GetMapping
    public ResponseEntity<?> getExpensesByUser(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "9") int elements,
                                                                 @RequestParam(defaultValue = "false") boolean fetchAll) {
        UserEntity user = getAuthenticatedUser();

        if (fetchAll) {
            // Obtén todos los gastos para el usuario
            List<ExpenseEntity> expenses = expenseService.getAllExpensesByUser(user.getIdUser());
            return ResponseEntity.ok(expenses);
        } else {
            // Si no se solicita todos, devuelve con paginación
            return ResponseEntity.ok(expenseService.getExpensesByUser(user.getIdUser(), page, elements));
        }
    }

    @GetMapping("/users/last-week")
    public ResponseEntity<Page<ExpenseEntity>> getExpensesForLastWeek(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "9") int elements) {

        UserEntity user = getAuthenticatedUser();
        return ResponseEntity.ok(expenseService.getExpenseForLastWeek(user.getUsername(), page, elements));
    }

    @GetMapping("/users/last-month")
    public ResponseEntity<Page<ExpenseEntity>> getExpensesForLastMonth(@RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "9") int elements) {
        UserEntity user = getAuthenticatedUser();
        return ResponseEntity.ok(expenseService.getExpenseForLastMonth(user.getUsername(), page, elements));
    }

    @GetMapping("/users/last-three-months")
    public ResponseEntity<Page<ExpenseEntity>> getExpensesForLastThreeMonths(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "9") int elements) {
        UserEntity user = getAuthenticatedUser();
        return ResponseEntity.ok(expenseService.getExpenseForLastThreeMonths(user.getUsername(), page, elements));
    }

    @GetMapping("/users/filter")
    public ResponseEntity<Page<ExpenseEntity>> getExpensesByDateRange(@RequestParam("startDate") LocalDate startDate,
                                                                      @RequestParam("endDate") LocalDate endDate,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "9") int elements) {
        UserEntity user = getAuthenticatedUser();
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(user.getUsername(), startDate, endDate, page, elements));
    }

    @PostMapping
    public ResponseEntity<ExpenseEntity> saveExpense(@RequestBody ExpenseDTO expenseDTO) {
        ExpenseEntity createdExpense = expenseService.createExpense(expenseDTO);
        return ResponseEntity.ok(createdExpense);
    }

    @PutMapping("/{idExpense}")
    public ResponseEntity<ExpenseEntity> updateExpense(@PathVariable Long idExpense,
                                                       @RequestBody ExpenseDTO expenseDTO) {
        ExpenseEntity updateExpense = expenseService.updateExpense(idExpense, expenseDTO);
        return ResponseEntity.ok(updateExpense);
    }

    @DeleteMapping("/{idExpense}")
    public ResponseEntity<Void> deleteExpense(@PathVariable long idExpense) {
        if (expenseService.exists(idExpense)) {
            expenseService.deleteExpense(idExpense);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}

package be.cytomine.controller.security;

import be.cytomine.controller.RestCytomineController;
import be.cytomine.dto.Account;
import be.cytomine.dto.Accounts;
import be.cytomine.exceptions.UserManagementException;
import be.cytomine.service.security.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
@Slf4j
@RequiredArgsConstructor
public class RestAccountController extends RestCytomineController {

    @Autowired
    AccountService accountService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Account account) throws UserManagementException {
        return accountService.createAccount(account);
    }

    @DeleteMapping("/{reference}")
    public ResponseEntity<?> delete(@PathVariable String reference) throws UserManagementException {
        return accountService.delete(reference);
    }

    @PutMapping("/{reference}")
    public ResponseEntity<?> update(@PathVariable String reference ,@RequestBody Account account) throws UserManagementException {
        account.setReference(reference);
        return accountService.update(account);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<?> get(@PathVariable String reference) throws UserManagementException {
        return accountService.find(reference);
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(defaultValue = "0") int offset,
                        @RequestParam(defaultValue = "100") int limit) throws UserManagementException {
        return accountService.find(offset , limit);
    }
}

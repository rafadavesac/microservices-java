package br.edu.atitus.productapi.servicesimpl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorizationService {

    // Tipo 0 = Admin no auth-api. No Lettera, é o Vendedor.
    public void requireSeller(Integer userType) {
        if (userType == null || userType != 0)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas vendedores podem executar esta operação");
    }
}
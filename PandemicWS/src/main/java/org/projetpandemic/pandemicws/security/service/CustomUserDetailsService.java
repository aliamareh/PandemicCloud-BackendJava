package org.projetpandemic.pandemicws.security.service;


import modele.Role;
import modele.exceptions.MauvaisLoginException;
import org.projetpandemic.pandemicws.modele.FacadePandemicImpl;
import org.projetpandemic.pandemicws.security.config.UsersAuthorities;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

public class CustomUserDetailsService implements UserDetailsService {

    private final FacadePandemicImpl facadePandemic;

    public CustomUserDetailsService(FacadePandemicImpl facadePandemic) {
        this.facadePandemic = facadePandemic;
    }

    /**
     * Génération dynamique des détails d'authentification d'un utilisateur, selon son username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Map<String,String> mapPseudoPassword;
        try {
            mapPseudoPassword = facadePandemic.getUserByPseudo(username);
        } catch (MauvaisLoginException e) {
            throw new UsernameNotFoundException(username);
        }

        return User.builder()
                .username(mapPseudoPassword.get("pseudo"))
                .password(mapPseudoPassword.get("password"))
                .roles(UsersAuthorities.USER.name())
                .build();
    }


}

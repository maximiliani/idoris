/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.idoris.users.web.v1;

import edu.kit.datamanager.idoris.users.entities.ORCiDUser;
import edu.kit.datamanager.idoris.users.entities.TextUser;
import edu.kit.datamanager.idoris.users.entities.User;
import edu.kit.datamanager.idoris.users.services.UserService;
import edu.kit.datamanager.idoris.users.web.api.IUserApi;
import edu.kit.datamanager.idoris.users.web.hateoas.UserModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for User entities.
 * This controller provides endpoints for managing User entities.
 */
@RestController
@RequestMapping("/v1/users")
public class UserController implements IUserApi {

    private final UserService userService;
    private final UserModelAssembler userModelAssembler;

    @Autowired
    public UserController(UserService userService, UserModelAssembler userModelAssembler) {
        this.userService = userService;
        this.userModelAssembler = userModelAssembler;
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<User>>> getAllUsers() {
        List<EntityModel<User>> users = userService.findAllUsers().stream()
                .map(userModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<User>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    @Override
    public ResponseEntity<EntityModel<User>> getUserById(String id) {
        return userService.findUserById(id)
                .map(userModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<TextUser>>> getAllTextUsers() {
        List<EntityModel<TextUser>> users = userService.findAllTextUsers().stream()
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getTextUserByEmail(user.getEmail())).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TextUser>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllTextUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @Override
    public ResponseEntity<EntityModel<TextUser>> getTextUserByEmail(String email) {
        return userService.findTextUserByEmail(email)
                .map(user -> EntityModel.of(user,
                        linkTo(methodOn(UserController.class).getTextUserByEmail(email)).withSelfRel(),
                        linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers"),
                        linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Text user not found"));
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<ORCiDUser>>> getAllORCiDUsers() {
        List<EntityModel<ORCiDUser>> users = userService.findAllORCiDUsers().stream()
                .map(user -> {
                    // Extract ORCID identifier from the URL
                    String orcidStr = user.getOrcid().toString().replace("https://orcid.org/", "");
                    return EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"));
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ORCiDUser>> collectionModel = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAllORCiDUsers()).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")
        );

        return ResponseEntity.ok(collectionModel);
    }

    @Override
    public ResponseEntity<EntityModel<ORCiDUser>> getORCiDUserByORCiD(String orcidStr) {
        try {
            // Convert ORCID string to URL
            URL orcid = URI.create("https://orcid.org/" + orcidStr).toURL();
            return userService.findORCiDUserByORCiD(orcid)
                    .map(user -> EntityModel.of(user,
                            linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                            linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"),
                            linkTo(methodOn(UserController.class).getAllUsers()).withRel("users")))
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ORCID user not found"));
        } catch (java.net.MalformedURLException e) {
            // Only catch MalformedURLException to return BAD_REQUEST
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ORCID format: " + orcidStr, e);
        }
    }

    @Override
    public ResponseEntity<EntityModel<TextUser>> createTextUser(TextUser user) {
        TextUser createdUser = userService.createTextUser(user);
        EntityModel<TextUser> entityModel = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getTextUserByEmail(createdUser.getEmail())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllTextUsers()).withRel("textUsers"),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Override
    public ResponseEntity<EntityModel<ORCiDUser>> createORCiDUser(ORCiDUser user) {
        ORCiDUser createdUser = userService.createORCiDUser(user);
        // Extract ORCID identifier from the URL
        String orcidStr = createdUser.getOrcid().toString().replace("https://orcid.org/", "");
        EntityModel<ORCiDUser> entityModel = EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getORCiDUserByORCiD(orcidStr)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAllORCiDUsers()).withRel("orcidUsers"),
                linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Override
    public ResponseEntity<EntityModel<User>> updateUser(String id, User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            EntityModel<User> entityModel = userModelAssembler.toModel(updatedUser);
            return ResponseEntity.ok(entityModel);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteUser(String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}

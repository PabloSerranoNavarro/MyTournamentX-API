package cat.udl.eps.softarch.mytournamentx.steps;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.softarch.mytournamentx.domain.User;
import cat.udl.eps.softarch.mytournamentx.repository.UserRepository;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class RegisterStepDefs {

  @Autowired private StepDefs stepDefs;
  @Autowired private UserRepository userRepository;

  @Given("^There is no registered user with username \"([^\"]*)\"$")
  public void thereIsNoRegisteredUserWithUsername(String username) {
    Assert.assertFalse("User \"" +  username + "\"shouldn't exist", userRepository.existsById(username));
  }

  @Given("^There is a registered user with username \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void thereIsARegisteredUserWithUsername(String username, String password) {
    if (!userRepository.existsById(username)) {
      User user = new User();
      user.setEmail(username + "@mytournamentx.game");
      user.setUsername(username);
      user.setPassword(password);
      user.encodePassword();
      userRepository.save(user);
    }
  }

  @When("^I register a new user with username \"([^\"]*)\", email \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iRegisterANewUser(String username, String email, String password) throws Throwable {
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);

    stepDefs.result = stepDefs.mockMvc.perform(
        post("/users")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(new JSONObject(
                  stepDefs.mapper.writeValueAsString(user)
                ).put("password", password).toString())
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print());
  }

  @And("^It has been created a user with username \"([^\"]*)\" and email \"([^\"]*)\", the password is not returned$")
  public void itHasBeenCreatedAUser(String username, String email) throws Throwable {
    stepDefs.result = stepDefs.mockMvc.perform(
        get("/users/{username}", username)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(jsonPath("$.email", is(email)))
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @And("^It has not been created a user with username \"([^\"]*)\"$")
  public void itHasNotBeenCreatedAUserWithUsername(String username) throws Throwable {
    stepDefs.result = stepDefs.mockMvc.perform(
        get("/users/{username}", username)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .with(AuthenticationStepDefs.authenticate()))
        .andExpect(status().isNotFound());
  }

  @And("^I can login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iCanLoginWithUsernameAndPassword(String username, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = username;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity", username)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @And("^I cannot login with username \"([^\"]*)\" and password \"([^\"]*)\"$")
  public void iCannotLoginWithUsernameAndPassword(String username, String password) throws Throwable {
    AuthenticationStepDefs.currentUsername = username;
    AuthenticationStepDefs.currentPassword = password;

    stepDefs.result = stepDefs.mockMvc.perform(
        get("/identity", username)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .with(AuthenticationStepDefs.authenticate()))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }
}
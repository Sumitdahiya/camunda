describe('cockpit dashboard', function() {

  function expectLoggedIn(username) {
    var loggedInUserMenu = element(by.binding('authentication.user.name'));
    expect(loggedInUserMenu.getText()).toEqual(username);
  }

  function login(username, password) {
    element(by.model('username')).sendKeys(username);
    element(by.model('password')).sendKeys(password);

    var submitButton = element(by.css('.btn-primary.btn-large'));
    submitButton.click();

    expectLoggedIn(username);
  }

  it('should show via valid credentials', function() {
    browser.get('http://localhost:8080/camunda/');

    login('jonny1', 'jonny1');
  });

  it('should fail login invalid credentials', function() {
    browser.get('http://localhost:8080/camunda/');

    var usernameInput = element(by.model('username'));
    
    usernameInput.sendKeys('not-found');
    usernameInput.sendKeys(protractor.Key.RETURN);
  });
});
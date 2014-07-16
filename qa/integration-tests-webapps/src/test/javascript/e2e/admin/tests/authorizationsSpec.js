'use strict';

var authorizationPage = require('../pages/authorizations');

describe('authorizations page - ', function() {

  describe('start test', function() {

    it('should login', function() {

      // given
      authorizationPage.navigateToWebapp('Admin');
      authorizationPage.login('demo', 'demo');

    });


    it('should validate URL', function() {

      // when
      authorizationPage.selectNavbarItem('Authorizations');

      // then
      authorizationPage.application.isActive();
      expect(authorizationPage.pageHeader()).toBe('Authorizations');

    });

  });


  describe('authorizations pages', function() {

    beforeEach(function() {

      // given
      authorizationPage.navigateToWebapp('Admin');
      authorizationPage.selectNavbarItem('Authorizations');

    });


    it('should validate application page', function() {

      // when
      authorizationPage.selectAuthorizationNavbarItem('Application');

      // then
      authorizationPage.application.isActive();
      expect(authorizationPage.application.createNewButton().isEnabled()).toBe(true);
      expect(authorizationPage.application.boxHeader()).toBe('Application Authorizations');

    });


    it('should validate authorization page', function() {

      // when
      authorizationPage.selectAuthorizationNavbarItem('Authorization');

      // then
      authorizationPage.authorization.isActive();
      expect(authorizationPage.authorization.createNewButton().isEnabled()).toBe(true);
      expect(authorizationPage.authorization.boxHeader()).toBe('Authorization Authorizations');

    });


    it('should validate group page', function() {

      // when
      authorizationPage.selectAuthorizationNavbarItem('Group');

      // then
      authorizationPage.group.isActive();
      expect(authorizationPage.group.createNewButton().isEnabled()).toBe(true);
      expect(authorizationPage.group.boxHeader()).toBe('Group Authorizations');

    });


    it('should validate group membership page', function() {

      // when
      authorizationPage.selectAuthorizationNavbarItem('Group Membership');

      // then
      authorizationPage.groupMembership.isActive();
      expect(authorizationPage.groupMembership.createNewButton().isEnabled()).toBe(true);
      expect(authorizationPage.groupMembership.boxHeader()).toBe('Group Membership Authorizations');

    });


    it('should validate user page', function() {

      // when
      authorizationPage.selectAuthorizationNavbarItem('User');

      // then
      authorizationPage.user.isActive();
      expect(authorizationPage.user.createNewButton().isEnabled()).toBe(true);
      expect(authorizationPage.user.boxHeader()).toBe('User Authorizations');

    });


  });


  describe('end test', function() {

    it('should log out', function() {

      authorizationPage.logoutWebapp();

    })

  });

});

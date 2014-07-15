'use strict';

var UsersPage = require('../pages/users/users');
var EditUserAccountPage = require('../pages/users/editUserAccount');
var EditUserGroupPage = require('../pages/users/editUserGroups');
var EditUserSelectGroupsPage = require('../pages/users/editUserSelectGroups');
var AdminUserSetupPage = require('../pages/users/adminSetup');

var usersPage = new UsersPage();
var editUserAccountPage = new EditUserAccountPage();
var editUserGroupPage = new EditUserGroupPage();
var editUserSelectGroupsPage = new EditUserSelectGroupsPage();
var adminUserSetupPage = new AdminUserSetupPage();

describe('admin user - ', function() {

  describe('start test', function() {

    it('should login', function() {

      usersPage.navigateToWebapp('Admin');

      usersPage.login('jonny1', 'jonny1');

    });

  });

  describe('remove current admin user rights', function() {

    it('should select user', function() {

      usersPage.selectUser(2);

      expect(editUserGroupPage.pageHeader()).toBe('Jonny Prosciutto')

    });

    it('should remove admin group and log out', function() {

      editUserGroupPage.selectUserNavbarItem('Groups');

      editUserGroupPage.removeGroup(0);

      expect(editUserGroupPage.groupList().count()).toEqual(0);

      editUserGroupPage.logoutWebapp();

    });

  });

  describe('validate intial admin setup', function() {

    it('should validate Setup page', function() {

      adminUserSetupPage.navigateToWebapp('Admin');

      expect(adminUserSetupPage.pageHeader()).toBe('Setup');

      expect(adminUserSetupPage.createNewAdminButton().isEnabled()).toBe(false);

    });

    it('should enter new admin profile', function() {

      adminUserSetupPage.userId().sendKeys('Admin');
      adminUserSetupPage.password().sendKeys('admin123');
      adminUserSetupPage.passwordRepeat().sendKeys('admin123');
      adminUserSetupPage.userFirstName().sendKeys('Über');
      adminUserSetupPage.userLastName().sendKeys('Admin');
      adminUserSetupPage.userEmail().sendKeys('uea@camundo.org');

      adminUserSetupPage.createNewAdminButton().click();

    });

    it('should login page as Admin', function() {

      usersPage.navigateToWebapp('Admin');

      usersPage.login('Admin', 'admin123');

    });

  });

  describe('reassign admin user rights', function() {

    it('should navigate to user groups settings', function() {

      usersPage.selectUser(3);

      editUserGroupPage.selectUserNavbarItem('Groups');

    });

    it('should open group select page', function() {

      editUserGroupPage.addGroupButton().click();

      expect(editUserSelectGroupsPage.pageHeader()).toBe('Select Groups');

    });

    it('should add camunda-admin group', function() {

      editUserSelectGroupsPage.addGroup(1);

      expect(editUserGroupPage.groupList().count()).toEqual(1);

    });

  });

  describe('remove interim admin', function() {

    it('should navigate to user account settings', function() {

      usersPage.navigateTo();

      usersPage.selectUser(0);

      editUserAccountPage.selectUserNavbarItem('Account');

    });

    it('should delete user account', function() {

      editUserAccountPage.deleteUserButton().click();

      editUserAccountPage.deleteUserAlert().accept();

      expect(usersPage.userList().count()).toEqual(5);

     });

  });

  describe('end test', function() {

    it('should log out', function() {

      usersPage.logoutWebapp();

    })

  });

});
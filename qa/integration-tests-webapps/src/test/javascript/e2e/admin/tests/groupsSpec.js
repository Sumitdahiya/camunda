'use strict';

var GroupsPage = require('../pages/groups/groups');
var NewGroupPage = require('../pages/groups/newGroup');
var EditGroupPage = require('../pages/groups/editGroup');

var groupsPage = new GroupsPage();
var newGroupPage = new NewGroupPage();
var editGroupPage = new EditGroupPage();

describe('groups page - ', function() {

  describe('start test', function() {

    it('should login', function() {
      groupsPage.navigateToWebapp('Admin');

      groupsPage.login('jonny1', 'jonny1');
    });

  });

  describe('groups main page', function() {

    beforeEach(function() {
      groupsPage.navigateTo();
    });

    it('should validate menu', function() {
      expect(groupsPage.pageHeader()).toBe('Groups');

      expect(groupsPage.newGroupButton().isPresent()).toBe(true);

      expect(groupsPage.groupList().count()).toBe(4);
    });

    it('should navigate to new group menu', function() {
      groupsPage.newGroupButton().click();

      newGroupPage.isActive();

      expect(newGroupPage.pageHeader()).toBe('Create New Group');
    });

  });

  describe('create new group menu', function() {

    beforeEach(function() {
      newGroupPage.navigateTo();
    });

    it('should validate new group menu', function() {
      expect(newGroupPage.createNewGroupButton().isEnabled()).toBe(false);

      newGroupPage.newGroupId().sendKeys('Hallo');
      newGroupPage.newGroupName().sendKeys('Gruppe');
      expect(newGroupPage.createNewGroupButton().isEnabled()).toBe(true);

      newGroupPage.newGroupId().clear();
      expect(newGroupPage.createNewGroupButton().isEnabled()).toBe(false);
      newGroupPage.newGroupName().clear();
      expect(newGroupPage.createNewGroupButton().isEnabled()).toBe(false);
    });

    it('should create new group', function() {
      newGroupPage.createNewGroup('4711', 'blaw', 'Marketing');

      expect(groupsPage.groupList().count()).toBe(5);
    });

  });

  describe('edit group menu', function() {

    it('should navigate to edit group menu', function() {
      groupsPage.navigateTo();

      groupsPage.editGroup(1);

      expect(editGroupPage.pageHeader()).toBe('Accounting');
    });

    it('should edit group', function() {
      editGroupPage.navigateTo({ group: '4711' });
      editGroupPage.isActive({ group: '4711' });

      expect(editGroupPage.updateGroupButton().isEnabled()).toBe(false);

      editGroupPage.groupName().sendKeys('i');

      editGroupPage.updateGroupButton().click();

      expect(editGroupPage.pageHeader()).toBe('blawi');
    });

    it('should delete group', function() {
      editGroupPage.deleteGroupButton().click();

      editGroupPage.deleteGroupAlert().accept();

      expect(groupsPage.groupList().count()).toBe(4);
    });
  });

});

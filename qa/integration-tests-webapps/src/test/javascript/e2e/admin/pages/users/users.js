'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/users',

  newUserButton: function() {
    return element(by.css('.btn.pull-right'));
  },

  userList: function() {
    return element.all(by.repeater('user in userList'));
  },

  userFirstNameAndLastName: function(item) {
    return element(by.repeater('user in userList').row(item).column('{{user.firstName}} {{user.lastName}}')).getText();
  },

  selectUser: function(item) {
    this.userList().get(item).findElement(by.linkText('Edit')).click();
  }

});

'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/admin/default/#/users',

  newUserButton: function () {
    return element(by.css('.btn.pull-right'));
  },

  userList: function () {
    return element.all(by.repeater('user in userList'));
  },

  selectUser: function (item) {
    this.userList().get(item).findElement(by.linkText('Edit')).click();
  }

});

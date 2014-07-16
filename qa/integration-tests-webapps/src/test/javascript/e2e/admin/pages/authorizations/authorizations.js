'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  /**
   Select Profile in users side navbar
   @memberof cam.test.e2e.admin.pages.editUser

   @param {string} navbarItem
   @return {!webdriver.promise.Promise}  - A promise of the selected element
   */
  selectAuthorizationNavbarItem: function(navbarItem) {
    var index = [
      'Application',
      'Authorization',
      'Group',
      'Group Membership',
      'User'
    ];
    var item;
    var itemIndex = index.indexOf(navbarItem) + 2;

    if (itemIndex)
      item = element(by.css('.sidebar-nav ul li:nth-child(' + itemIndex + ')'));
    else
      item = element(by.css('.sidebar-nav ul li:nth-child(1)'));

    item.click();
    return item;
  },

  boxHeader: function() {
    return element(by.css('[ng-controller="AuthorizationCreateController"] legend')).getText();
  },

  createNewButton: function() {
    return element(by.css('.btn.btn-link'));
  }

});
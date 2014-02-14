/* global exports: true */
'use strict';
exports.config = {
  seleniumJar: './../../../../selenium/selenium-server-standalone-2.39.0.jar',

  specs: [
    '../e2e{,/**}/*Test.js'
  ]
};

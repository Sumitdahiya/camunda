/* global process: false, require: false, module: false, __dirname: false, console: false */
'use strict';
/* jshint unused: false */
var spawn = require('child_process').spawn;
var path = require('path');
var fs = require('fs');
var _ = require('underscore');

var commentLineExp =  /^[\s]*<!-- (\/|#) (CE|EE)/;
var htmlFileExp =     /\.html$/;
var requireConfExp =  /require-conf.js$/;
// var seleniumJarExp = /\.jar downloaded to (.+)$/gm;
var seleniumJarNameExp = /selenium-server-standalone/;
// var seleniumExp = /(.+)\/([^\/]+)/;
var seleniumServerJarPath;

function distFileProcessing(content, srcpath) {
  if (htmlFileExp.test(srcpath)) {
    // removes the template comments
    content = content
              .split('\n').filter(function(line) {
                return !commentLineExp.test(line);
              }).join('\n');
  }

  return content;
}

function standaloneSeleniumJar() {
  if (!fs.existsSync('selenium')) {
    return false;
  }
  var filename = _.find(fs.readdirSync('selenium'), function(filename) {
    return seleniumJarNameExp.test(filename);
  });
  return path.join(__dirname, 'selenium', filename);
}

module.exports = function(grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  var packageJSON = grunt.file.readJSON('package.json');

  // Project configuration.
  grunt.initConfig({
    pkg: packageJSON,

    app: {
      port: parseInt(process.env.APP_PORT || 8080, 10),
      liveReloadPort: parseInt(process.env.LIVERELOAD_PORT || 8081, 10),
      standaloneSeleniumJar: standaloneSeleniumJar
    },

    clean: {
      target: [
        'target/webapp'
      ],
      assets: [
        'target/webapp/assets'
      ]
    },

    copy: {
      development: {
        files: [
          {
            expand: true,
            cwd: 'src/main/webapp/WEB-INF',
            src: ['*'],
            dest: 'target/webapp/WEB-INF'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              'require-conf.js',
              'index.html'
            ],
            dest: 'target/webapp/'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              '{app,plugin,develop,common}/**/*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: function(content, srcpath) {
            // Unfortunately, this might (in some cases) make angular complaining
            // about template having no single root element
            // (when the "replace" option is set to "true").

            // if (htmlFileExp.test(srcpath)) {
            //   content = '<!-- # CE - auto-comment - '+ srcpath +' -->\n'+
            //             content +
            //             '\n<!-- / CE - auto-comment - '+ srcpath +' -->';
            // }

            if (requireConfExp.test(srcpath)) {
              content = content
                // .replace(/CACHE_BUST/, 'bust='+ (new Date()).getTime())
                .replace(/\/\* live-reload/, '/* live-reload */')
                .replace(/LIVERELOAD_PORT/g, grunt.config('app.liveReloadPort'));
            }

            return content;
          }
        }
      },
      dist: {
        files: [
          {
            expand: true,
            cwd: 'src/main/webapp/WEB-INF',
            src: ['*'],
            dest: 'target/webapp/WEB-INF'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              'require-conf.js',
              'index.html'
            ],
            dest: 'target/webapp/'
          },
          {
            expand: true,
            cwd: 'src/main/webapp/',
            src: [
              '{app,plugin,develop,common}/**/*.{js,html}'
            ],
            dest: 'target/webapp/'
          }
        ],
        options: {
          process: distFileProcessing
        }
      },

      assets: {
        files: [
          // requirejs
          {
            src: 'src/main/webapp/assets/vendor/requirejs/index.js',
            dest: 'target/webapp/assets/vendor/requirejs/require.js'
          },
          // others
          {
            expand: true,
            cwd: 'src/main/webapp/assets',
            src: [
              '!vendor/requirejs/**/*',
              'css/**/*',
              'img/**/*',
              'vendor/**/*.{js,css,jpg,png,gif,html,eot,ttf,svg,woff}'
            ],
            dest: 'target/webapp/assets'
          }
        ]
      }
    },

    watch: {
      options: {
        livereload: false
      },

      // watch for source script changes
      scripts: {
        files: [
          'src/main/webapp/require-conf.js',
          'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}'
        ],
        tasks: [
          'newer:jshint:scripts',
          'newer:copy:development'
        ]
      },

      tests: {
        files: [
          'src/main/webapp/require-conf.js',
          'src/main/webapp/{app,develop,plugin,common}/**/*.{js,html}',
          'src/test/js/{config,e2e,test,unit}/**/*.js'
        ],
        tasks: [
          'newer:jshint:test',
          'karma:test',
          'karma:unit'
        ]
      },

      styles: {
        files: [
          'src/main/webapp/assets/styles/**/*.less'
        ],
        tasks: [
          'less:development'
        ]
      },

      servedAssets: {
        options: {
          livereload: '<%= app.liveReloadPort %>'
        },
        files: [
          'target/webapp/assets/{css,img}/**/*.{css,jpg,png,html}',
          'target/webapp/{app,plugin,develop}/**/*.{css,js,html,jpg,png}'
        ],
        tasks: []
      }
    },

    jshint: {
      options: {
        browser: true,
        // globals: {
        //   angular: true,
        //   jQuery: true
        // }
      },
      test: {
        files: {
          src: [
            'test/js/{config,e2e,unit}/**/*.js'
          ]
        }
      },
      scripts: {
        files: {
          src: [
            'Gruntfile.js',
            'src/main/webapp/{app,assets,develop,plugin}/**/*.js'
          ]
        }
      }
    },

    karma: {
      // to test the testing environment
      test: {
        configFile: 'src/test/js/config/karma.test.js'
      },

      unit: {
        configFile: 'src/test/js/config/karma.unit.js'
      },

      testWatched: {
        singleRun: false,
        autoWatch: true,
        configFile: 'src/test/js/config/karma.test.js',
        browsers: ['Chrome', 'Firefox']
      },

      unitWatched: {
        singleRun: false,
        autoWatch: true,
        configFile: 'src/test/js/config/karma.unit.js',
        browsers: ['Chrome', 'Firefox']
      }
    },

    protractor: {
      options: {
        singleRun: true,
        args: {
          // use a function(!!!) to determine the path of selenium standalone web-driver
          seleniumServerJar: '<%= app.standaloneSeleniumJar() %>',
          specs: [
            'src/test/js/e2e/**/*Test.js'
          ]
        }
      },

      e2e: {}
    },

    jsdoc : {
      dist : {
        src: [
          'README.md',
          'src/main/webapp/require-conf.js',
          'src/main/webapp/app',
          'src/main/webapp/develop',
          'src/main/webapp/plugin'
        ],

        options: {
          // grunt-jsdoc has a big problem... some kind of double-parsing...
          // using the `jsdoc -d doc -r -c jsdoc-conf.json` command works fine
          // configure: './jsdoc-conf.json',
          destination: 'doc'
        }
      }
    },

    bower: {
      install: {}
    },

    less: {
      options: {
        // paths: []
      },

      dist: {
        options: {
          cleancss: true
        },
        files: {
          'target/webapp/assets/css/common.css': 'src/main/webapp/assets/styles/common.less',
          'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/assets/styles/cockpit/loader.less',
          'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/assets/styles/admin/loader.less',
          'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/assets/styles/tasklist/loader.less'
        }
      },

      development: {
        files: {
          'target/webapp/assets/css/common.css': 'src/main/webapp/assets/styles/common.less',
          'target/webapp/assets/css/cockpit/loader.css': 'src/main/webapp/assets/styles/cockpit/loader.less',
          'target/webapp/assets/css/admin/loader.css': 'src/main/webapp/assets/styles/admin/loader.less',
          'target/webapp/assets/css/tasklist/loader.css': 'src/main/webapp/assets/styles/tasklist/loader.less'
        }
      }
    },

    open: {
      server: {
        url: 'http://localhost:<%= app.port %>/camunda'
      }
    }
  });

  grunt.registerTask('selenium-install', 'Automate the selenium webdriver installation', function() {
    var done = this.async();
    var stdout = '';
    var stderr = '';

    var managerPath = './node_modules/grunt-protractor-runner/node_modules/protractor/bin/webdriver-manager';
    var args = [
      'update',
      '--out_dir',
      __dirname +'/selenium'
    ];

    grunt.log.writeln('selenium-install runs: '+ managerPath +' '+ args.join(' '));

    var install = spawn(managerPath, args);

    install.stdout.on('data', function(data) { stdout += data; });
    install.stderr.on('data', function(data) { stderr += data; });

    install.on('exit', function (code) {
      if (code) {
        return done(new Error('selenium-install exit with code: '+ code));
      }

      grunt.log.writeln('selenium standalone server installed');

      // // updates the configuration of protractor
      // // with the right version of selenium standalone web-driver
      // var originalProtractorConf = grunt.config.getRaw('protractor');
      // originalProtractorConf.options.args.seleniumServerJar = standaloneSeleniumJar();
      // grunt.config('protractor', originalProtractorConf);

      // console.info('grunt', [[grunt.config]], grunt.config('protractor'));
      done();
    });
  });

  // automatically (re-)build web assets
  grunt.registerTask('auto-build', 'Continuously (re-)build front-end assets', function (target) {
    if (target === 'dist') {
      throw new Error('dist target not yet supported');
    }

    grunt.task.run([
      'build:development',
      'open',
      'watch'
    ]);
  });

  grunt.registerTask('test', 'Run the tests (by default: karma:unit)', function(target) {
    var tasks = [];

    switch (target) {
      // test the testing environment
      case 'test':
        tasks.push('karma:test');
        break;

      // should use protractor
      case 'e2e':
        tasks.push('selenium-install');
        tasks.push('protractor');
        break;

      // unit testing by default
      default:
        tasks.push('karma:unit');
    }


    return grunt.task.run(tasks);
  });

  // Aimed to hold more complex build processes
  grunt.registerTask('build', 'Build the frontend assets', function(target) {
    var tasks = [
      'clean',
      'bower'
    ];

    if (target === 'dist') {
      // TODO: minification using ngr:
      // - Minifaction: https://app.camunda.com/jira/browse/CAM-1667
      // - Bug in ngDefine: https://app.camunda.com/jira/browse/CAM-1713

      tasks = tasks.concat([
        'copy:assets',
        'copy:dist'
      ]);
    }


    tasks = tasks.concat([
      'less:'+ target,
      'newer:copy:assets',
      'newer:copy:'+ target
    ]);

    return grunt.task.run(tasks);
  });

  // Default task(s).
  grunt.registerTask('default', ['build:dist']);
};

ngDefine('camunda.common.directives', [ 'ZeroClipboard', 'require' ], function(module, ZeroClipboard, require) {
  
  ZeroClipboard.setDefaults({
    moviePath: require.toUrl('ZeroClipboard/../ZeroClipboard.swf')
  });

  module.directive('clip', [ '$interpolate', function($interpolate) {
    return {
      link: function(scope, element, attrs) {
        var title = attrs['clipTitle'] || "Copy",
            load = attrs['clipLoad'] ? function() { return scope.$eval(attrs['clipLoad']); } : null;

        var copyButton = $('<button href class="btn btn-link"><i class="icon-share"></i></button>');

        if (!isInput(element)) {
          copyButton.addClass('btn-link');
        }

        copyButton.insertAfter(element);
        copyButton.tooltip({ placement: 'bottom', container: 'body', trigger: 'hover', title: title });

        function isInput(element) {
          return element.is('input, textarea');
        }

        function showTooltip(text) {
          copyButton.tooltip('show');
        }

        function registerClipLoad(load) {
          function copyButtonHover() {
            var result = load(), 
                icon = copyButton.find('i');

            if (result.then) {
              icon.addClass('icon-loading');

              result.then(function(text) {
                icon.removeClass('icon-loading');
                createClip(text);

                showTooltip('Ready!');
              }, function(error) {
                console.log(error);
              });
            } else {
              createClip(result);
            }

            copyButton.off('mouseover', copyButtonHover);
          }

          copyButton.on('mouseover', copyButtonHover);
        }

        copyButton.on('clip.mouseover', function() {
          console.log("mouseover");
          // copyButton.tooltip('show');
        });

        copyButton.on('clip.mouseout', function() {
          console.log("mouseout");
          // copyButton.tooltip('hide');
        });

        copyButton.on('clip.complete', function() {
          console.log("CLIP COMPLETE");
          // showTooltip('Copied to clipboard');
        });

        function createClip(text) {

          console.log("create clip", text);

          var clip = new ZeroClipboard(copyButton);

          if (text) {
            clip.setText(text);
          }

          clip.on('complete', function(client, args) {
            copyButton.trigger('clip.complete');
          });

          clip.on('mouseover', function(client, args) {
            copyButton.trigger('clip.mouseover');
          });

          clip.on('mouseout', function(client, args) {
            copyButton.trigger('clip.mouseout');
          });
        }

        if (load) {
          registerClipLoad(load);
        } else {

          var value = attrs['clipText'] ? $interpolate(attrs['clipText'])(scope) : null;
          if (!value) {
            value = isInput(element) ? element.val() : element.text();
          }

          createClip(value);
        }
      }
    }
  }]);
});
/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;
/******/
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ function(module, exports, __webpack_require__) {

	__webpack_require__(1);

	__webpack_require__(2);

	__webpack_require__(3);

	__webpack_require__(4);

	__webpack_require__(5);

	__webpack_require__(6);

	__webpack_require__(7);

	__webpack_require__(8);

	__webpack_require__(9);

	__webpack_require__(10);

	__webpack_require__(11);

	__webpack_require__(12);

	__webpack_require__(13);

	__webpack_require__(14);

	__webpack_require__(15);

	__webpack_require__(16);


/***/ },
/* 1 */
/***/ function(module, exports, __webpack_require__) {

	var mod;

	mod = __webpack_require__(18);

	mod.provider('fsConfig', function() {
	  this.$get = function() {
	    return {};
	  };
	  return this;
	});


/***/ },
/* 2 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(23);

	u = __webpack_require__(19);

	__webpack_require__(37);

	mod.directive("fsRadio", [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: "A",
	      scope: {
	        required: '=',
	        disabled: '=ngDisabled',
	        items: '=',
	        inline: '=',
	        keyAttr: '@',
	        valueAttr: '@'
	      },
	      require: '?ngModel',
	      template: function(el, attrs) {
	        var itemTpl, name;
	        itemTpl = el.html() || '{{item.label}}';
	        name = "fsRadio_" + (u.nextUid());
	        return $templateCache.get('templates/fs/metaRadio.html').replace(/::name/g, name).replace(/::itemTpl/g, itemTpl);
	      },
	      link: function(scope, element, attrs, ngModelCtrl, transcludeFn) {
	        if (ngModelCtrl) {
	          scope.$watch('selectedItem', function(newValue, oldValue) {
	            if (newValue !== oldValue) {
	              return ngModelCtrl.$setViewValue(scope.selectedItem);
	            }
	          });
	          return ngModelCtrl.$render = function() {
	            return scope.selectedItem = ngModelCtrl.$modelValue;
	          };
	        }
	      }
	    };
	  }
	]);


/***/ },
/* 3 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(25);

	__webpack_require__(38);

	u = __webpack_require__(19);

	mod.directive("fsList", [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: "A",
	      scope: {
	        items: '=',
	        "class": '@',
	        listInterface: '='
	      },
	      replace: true,
	      template: function(el, attrs) {
	        var itemTpl;
	        itemTpl = el.html() || 'template me: {{item | json}}';
	        return $templateCache.get('templates/fs/list.html').replace(/::itemTpl/g, itemTpl);
	      },
	      link: function($scope, $element, $attrs) {
	        var ensureHighlightedItemVisible;
	        ensureHighlightedItemVisible = function() {
	          var delayedScrollFn;
	          delayedScrollFn = function() {
	            var li, ul;
	            ul = $element.find('ul')[0];
	            li = ul.querySelector('li.active');
	            return u.scrollToTarget(ul, li);
	          };
	          return setTimeout(delayedScrollFn, 0);
	        };
	        return $scope.$watch('highlightIndex', function(idx) {
	          return ensureHighlightedItemVisible();
	        });
	      },
	      controller: [
	        '$scope', '$element', '$attrs', '$filter', function($scope, $element, $attrs, $filter) {
	          var updateSelectedItem;
	          updateSelectedItem = function(hlIdx) {
	            if ($scope.$parent.listInterface != null) {
	              return $scope.$parent.listInterface.selectedItem = $scope.items[hlIdx];
	            }
	          };
	          $scope.highlightItem = function(item) {
	            $scope.highlightIndex = $scope.items.indexOf(item);
	            if ($scope.$parent.listInterface != null) {
	              return $scope.$parent.listInterface.onSelect(item);
	            }
	          };
	          $scope.$watch('items', function(newItems) {
	            $scope.highlightIndex = 0;
	            return updateSelectedItem(0);
	          });
	          $scope.$watch('highlightIndex', function(idx) {
	            return updateSelectedItem(idx);
	          });
	          $scope.move = function(d) {
	            var items;
	            items = $scope.items;
	            $scope.highlightIndex += d;
	            if ($scope.highlightIndex === -1) {
	              $scope.highlightIndex = items.length - 1;
	            }
	            if ($scope.highlightIndex >= items.length) {
	              return $scope.highlightIndex = 0;
	            }
	          };
	          $scope.highlightIndex = 0;
	          if ($scope.$parent.listInterface != null) {
	            return $scope.$parent.listInterface.move = function(delta) {
	              return $scope.move(delta);
	            };
	          }
	        }
	      ]
	    };
	  }
	]);


/***/ },
/* 4 */
/***/ function(module, exports, __webpack_require__) {

	var parentsUntil;

	parentsUntil = function(element, cssClass) {
	  var el;
	  el = element;
	  while (el && !el.hasClass(cssClass)) {
	    el = el.parent();
	  }
	  if (!el || !el.hasClass(cssClass)) {
	    return nil;
	  } else {
	    return el;
	  }
	};

	angular.module("formstamp").directive("fsInput", [
	  '$parse', function($parse) {
	    return {
	      restrict: "A",
	      link: function(scope, element, attrs) {
	        var blurElement, focusElement, fsRoot, keyCodes;
	        focusElement = function() {
	          return setTimeout((function() {
	            return element[0].focus();
	          }), 0);
	        };
	        blurElement = function() {
	          return setTimeout((function() {
	            return element[0].blur();
	          }), 0);
	        };
	        keyCodes = {
	          Tab: 9,
	          ShiftTab: 9,
	          Enter: 13,
	          Esc: 27,
	          PgUp: 33,
	          PgDown: 34,
	          Left: 37,
	          Up: 38,
	          Right: 39,
	          Down: 40,
	          Space: 32,
	          Backspace: 8
	        };
	        if (attrs["fsFocusWhen"] != null) {
	          scope.$watch(attrs["fsFocusWhen"], function(newValue) {
	            if (newValue) {
	              return focusElement();
	            }
	          });
	        }
	        if (attrs["fsBlurWhen"] != null) {
	          scope.$watch(attrs["fsBlurWhen"], function(newValue) {
	            if (newValue) {
	              return blurElement();
	            }
	          });
	        }
	        if (attrs["fsOnFocus"] != null) {
	          element.on('focus', function(event) {
	            return scope.$apply(attrs["fsOnFocus"]);
	          });
	        }
	        if (attrs["fsOnBlur"] != null) {
	          element.on('blur', function(event) {
	            return scope.$apply(attrs["fsOnBlur"]);
	          });
	        }
	        if (attrs["fsHoldFocus"] != null) {
	          fsRoot = parentsUntil(element, "fs-widget-root");
	          fsRoot.on("mousedown", function(event) {
	            if (event.target !== element[0]) {
	              event.preventDefault();
	              return false;
	            } else {
	              return true;
	            }
	          });
	        }
	        return angular.forEach(keyCodes, function(keyCode, keyName) {
	          var attrName, callbackExpr, shift;
	          attrName = 'fs' + keyName;
	          if (attrs[attrName] != null) {
	            shift = keyName.indexOf('Shift') !== -1;
	            callbackExpr = $parse(attrs[attrName]);
	            return element.on('keydown', function(event) {
	              if (event.keyCode === keyCode && event.shiftKey === shift) {
	                if (!scope.$apply(function() {
	                  return callbackExpr(scope, {
	                    $event: event
	                  });
	                })) {
	                  return event.preventDefault();
	                }
	              }
	            });
	          }
	        });
	      }
	    };
	  }
	]);


/***/ },
/* 5 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(23);

	u = __webpack_require__(19);

	__webpack_require__(39);

	mod.directive("fsCheckbox", [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: "A",
	      scope: {
	        disabled: '=ngDisabled',
	        required: '=',
	        errors: '=',
	        items: '=',
	        inline: '='
	      },
	      require: '?ngModel',
	      replace: true,
	      template: function(el, attrs) {
	        var itemTpl;
	        itemTpl = el.html() || 'template me: {{item | json}}';
	        return $templateCache.get('templates/fs/metaCheckbox.html').replace(/::itemTpl/g, itemTpl);
	      },
	      controller: [
	        '$scope', '$element', '$attrs', function($scope, $element, $attrs) {
	          $scope.toggle = function(item) {
	            if ($scope.disabled) {
	              return;
	            }
	            if (!$scope.isSelected(item)) {
	              $scope.selectedItems.push(item);
	            } else {
	              $scope.selectedItems.splice(u.indexOf($scope.selectedItems, item), 1);
	            }
	            return false;
	          };
	          $scope.isSelected = function(item) {
	            return u.indexOf($scope.selectedItems, item) > -1;
	          };
	          $scope.invalid = function() {
	            return ($scope.errors != null) && $scope.errors.length > 0;
	          };
	          return $scope.selectedItems = [];
	        }
	      ],
	      link: function(scope, element, attrs, ngModelCtrl, transcludeFn) {
	        var setViewValue;
	        if (ngModelCtrl) {
	          setViewValue = function(newValue, oldValue) {
	            if (!angular.equals(newValue, oldValue)) {
	              return ngModelCtrl.$setViewValue(scope.selectedItems);
	            }
	          };
	          scope.$watch('selectedItems', setViewValue, true);
	          return ngModelCtrl.$render = function() {
	            if (!scope.disabled) {
	              return scope.selectedItems = ngModelCtrl.$viewValue || [];
	            }
	          };
	        }
	      }
	    };
	  }
	]);


/***/ },
/* 6 */
/***/ function(module, exports, __webpack_require__) {

	var VALIDATION_DIRECTIVES, mod;

	mod = __webpack_require__(18);

	VALIDATION_DIRECTIVES = ['ngRequired', 'ngMinlength', 'ngMaxlength', 'ngPattern', 'ngDisabled'];

	__webpack_require__(40);

	mod.directive('fsField', function() {
	  return {
	    restrict: 'A',
	    replace: true,
	    require: ['^fsFormFor', '^form'],
	    templateUrl: 'templates/fs/field.html',
	    scope: {
	      items: '=',
	      field: '@fsField',
	      type: '@',
	      label: '@'
	    },
	    compile: function(tElement, tAttrs) {
	      var inputDiv, inputDivRaw, type;
	      type = tAttrs.type;
	      inputDivRaw = tElement[0].querySelector('.fs-field-input');
	      inputDiv = angular.element(inputDivRaw);
	      angular.element(inputDiv).attr(type, '');
	      angular.forEach(VALIDATION_DIRECTIVES, function(dir) {
	        if (tAttrs[dir]) {
	          return inputDiv.attr(tAttrs.$attr[dir], tAttrs[dir]);
	        }
	      });
	      inputDiv.attr('name', tAttrs.fsField);
	      return function(scope, element, attrs, ctrls) {
	        var formCtrl, formForCtrl;
	        formForCtrl = ctrls[0];
	        formCtrl = ctrls[1];
	        scope.object = formForCtrl.getObject();
	        scope.objectName = formForCtrl.getObjectName();
	        formCtrl = element.parent().controller('form');
	        scope.defaultErrors = {
	          'required': 'This field is required!',
	          'pattern': 'This field should match pattern!',
	          'minlength': 'This field should be longer!',
	          'maxlength': 'This field should be shorter!'
	        };
	        scope.hasErrorFor = function(validityName) {
	          return formCtrl[scope.field].$error[validityName];
	        };
	        return scope.$watch(function() {
	          var errs;
	          if (!formCtrl.$dirty) {
	            return;
	          }
	          scope.validationErrors = [];
	          angular.forEach(scope.defaultErrors, function(value, key) {
	            if (scope.hasErrorFor(key)) {
	              return scope.validationErrors.push(value);
	            }
	          });
	          if (scope.object.$error && (errs = scope.object.$error[scope.field])) {
	            scope.validationErrors = scope.validationErrors.concat(errs);
	          }
	          console.log(scope.validationErrors);
	        });
	      };
	    }
	  };
	});


/***/ },
/* 7 */
/***/ function(module, exports, __webpack_require__) {

	var formNameCounter, isDirectChild, mod, nextFormName, setAttrs,
	  __hasProp = {}.hasOwnProperty;

	mod = __webpack_require__(18);

	__webpack_require__(41);

	__webpack_require__(42);

	__webpack_require__(43);

	setAttrs = function(el, attrs) {
	  var attr, value, _results;
	  _results = [];
	  for (attr in attrs) {
	    if (!__hasProp.call(attrs, attr)) continue;
	    value = attrs[attr];
	    _results.push(el.attr(attr, value || true));
	  }
	  return _results;
	};

	mod.directive('fsErrors', [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: 'A',
	      scope: {
	        model: '='
	      },
	      replace: true,
	      template: $templateCache.get('templates/fs/errors.html'),
	      controller: [
	        '$scope', function($scope) {
	          var errorsWatcher, makeMessage;
	          makeMessage = function(idn) {
	            return "Error happened: " + idn;
	          };
	          errorsWatcher = function(newErrors) {
	            var errorIdn, occured;
	            return $scope.messages = (function() {
	              var _results;
	              _results = [];
	              for (errorIdn in newErrors) {
	                occured = newErrors[errorIdn];
	                if (occured) {
	                  _results.push(makeMessage(errorIdn));
	                }
	              }
	              return _results;
	            })();
	          };
	          return $scope.$watch('model.$error', errorsWatcher, true);
	        }
	      ]
	    };
	  }
	]);

	isDirectChild = function(form, el) {
	  var testel;
	  testel = el;
	  while (testel) {
	    if (testel.attributes.getNamedItem('fs-form-for')) {
	      if (testel.isSameNode(form)) {
	        return true;
	      } else {
	        return false;
	      }
	    }
	    testel = testel.parentNode;
	  }
	  return false;
	};

	formNameCounter = 1;

	nextFormName = function() {
	  return "autoGeneratedFormName" + (formNameCounter++);
	};

	mod.directive('fsFormFor', [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: 'EA',
	      replace: false,
	      template: function(el, attrs) {
	        var formName, input, inputReplacer, inputTpl, modelName, root, row, rowReplacer, rowTpl, template, tplEl, _i, _j, _len, _len1, _ref, _ref1;
	        if (el[0].tagName === "FS-FORM-FOR") {
	          template = "<h3 style=\"color: red\">DEPRECATED: &lt;fs-form-for&gt;&lt;/fs-form-for&gt;</h3> <p><code>&lt;fs-form-for&gt;</code> usage as element was deprecated. Use <code>&lt;form fs-form-for&gt;</code> instead.</p>";
	        } else {
	          inputTpl = $templateCache.get('templates/fs/metaInput.html');
	          rowTpl = $templateCache.get('templates/fs/metaRow.html');
	          modelName = el.attr("model");
	          formName = attrs.name || nextFormName();
	          attrs.$set('name', formName);
	          inputReplacer = function(el) {
	            var attr, attributes, input, inputEl, label, name, type, _i, _len, _ref;
	            input = angular.element(el);
	            name = input.attr("name");
	            type = input.attr("as");
	            label = input.attr("label") || name;
	            attributes = {};
	            _ref = input.prop("attributes");
	            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
	              attr = _ref[_i];
	              attributes[attr.name] = attr.value;
	            }
	            attributes['ng-model'] = "" + modelName + "." + name;
	            attributes['name'] = name;
	            delete attributes['as'];
	            if (type.indexOf("fs-") === 0) {
	              attributes[type] = true;
	              inputEl = angular.element("<div />");
	              setAttrs(inputEl, attributes);
	              inputEl.html(input.html());
	            } else if (type === 'textarea') {
	              attributes[type] = true;
	              attributes['class'] = 'form-control';
	              inputEl = angular.element("<textarea />");
	              setAttrs(inputEl, attributes);
	              inputEl.html(input.html());
	            } else {
	              attributes['type'] = type;
	              attributes['class'] = 'form-control';
	              inputEl = angular.element("<input />");
	              setAttrs(inputEl, attributes);
	            }
	            return inputTpl.replace(/::formName/g, formName).replace(/::name/g, name).replace(/::label/g, label).replace(/::content/, inputEl[0].outerHTML);
	          };
	          rowReplacer = function(el) {
	            var label, row;
	            row = angular.element(el);
	            label = row.attr("label");
	            return rowTpl.replace(/::label/g, label).replace(/::content/, row.html());
	          };
	          tplEl = el.clone();
	          root = tplEl[0];
	          _ref = tplEl.find("fs-input");
	          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
	            input = _ref[_i];
	            if (isDirectChild(root, input)) {
	              angular.element(input).replaceWith(inputReplacer(input));
	            }
	          }
	          _ref1 = tplEl.find("fs-row");
	          for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
	            row = _ref1[_j];
	            angular.element(row).replaceWith(rowReplacer(row));
	          }
	          template = tplEl.html();
	        }
	        return template;
	      }
	    };
	  }
	]);


/***/ },
/* 8 */
/***/ function(module, exports, __webpack_require__) {

	var mod;

	mod = __webpack_require__(18);

	__webpack_require__(27);

	__webpack_require__(29);

	__webpack_require__(44);

	mod.directive("fsSelect", [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: "A",
	      scope: {
	        items: '=',
	        disabled: '=ngDisabled',
	        freetext: '@',
	        "class": '@'
	      },
	      require: '?ngModel',
	      replace: true,
	      template: function(el) {
	        var itemTpl;
	        itemTpl = el.html();
	        return $templateCache.get('templates/fs/metaSelect.html').replace(/::itemTpl/g, itemTpl);
	      },
	      controller: [
	        '$scope', '$element', '$attrs', '$filter', '$timeout', function($scope, $element, $attrs, $filter, $timeout) {
	          var throttleTime, updateDropdown;
	          $scope.asyncState = 'loaded';
	          $scope.active = false;
	          throttleTime = parseInt($attrs['throttle'] || '200');
	          if ($attrs.freetext != null) {
	            $scope.dynamicItems = function() {
	              if ($scope.search) {
	                return [$scope.search];
	              } else {
	                return [];
	              }
	            };
	          } else {
	            $scope.dynamicItems = function() {
	              return [];
	            };
	          }
	          updateDropdown = function() {
	            var result;
	            if (angular.isFunction($scope.items)) {
	              result = $scope.items($scope.search);
	              if (angular.isArray(result)) {
	                return $scope.dropdownItems = result;
	              } else {
	                $scope.asyncState = 'loading';
	                return result.then(function(data) {
	                  $scope.asyncState = 'loaded';
	                  return $scope.dropdownItems = data;
	                }, function(data) {
	                  console.log("WARNING: promise rejected");
	                  $scope.asyncState = 'loaded';
	                  return $scope.dropdownItems = [];
	                });
	              }
	            } else {
	              return $scope.dropdownItems = $filter('filter')($scope.items || [], $scope.search).concat($scope.dynamicItems());
	            }
	          };
	          $scope.$watch('active', function(q) {
	            return updateDropdown();
	          });
	          $scope.$watch('items', function(q) {
	            return $scope.dropdownItems = [];
	          });
	          $scope.$watch('search', function(q) {
	            if (angular.isFunction($scope.items)) {
	              if ($scope.searchTimeout) {
	                $timeout.cancel($scope.searchTimeout);
	              }
	              return $scope.searchTimeout = $timeout(updateDropdown, throttleTime);
	            } else {
	              return updateDropdown();
	            }
	          });
	          $scope.selectItem = function(item) {
	            $scope.item = item;
	            $scope.search = "";
	            return $scope.active = false;
	          };
	          $scope.unselectItem = function(item) {
	            return $scope.item = null;
	          };
	          $scope.onBlur = function() {
	            return $timeout(function() {
	              $scope.active = false;
	              return $scope.search = '';
	            }, 0, true);
	          };
	          $scope.move = function(d) {
	            return $scope.listInterface.move && $scope.listInterface.move(d);
	          };
	          $scope.onEnter = function(event) {
	            if ($scope.dropdownItems.length > 0) {
	              return $scope.selectItem($scope.listInterface.selectedItem);
	            } else {
	              return $scope.selectItem(null);
	            }
	          };
	          return $scope.listInterface = {
	            onSelect: function(selectedItem) {
	              return $scope.selectItem(selectedItem);
	            },
	            move: function() {
	              return console.log("not-implemented listInterface.move() function");
	            }
	          };
	        }
	      ],
	      link: function(scope, element, attrs, ngModelCtrl, transcludeFn) {
	        if (ngModelCtrl) {
	          scope.$watch('item', function(newValue, oldValue) {
	            if (newValue !== oldValue) {
	              return ngModelCtrl.$setViewValue(scope.item);
	            }
	          });
	          return ngModelCtrl.$render = function() {
	            return scope.item = ngModelCtrl.$viewValue;
	          };
	        }
	      }
	    };
	  }
	]);


/***/ },
/* 9 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(27);

	__webpack_require__(31);

	__webpack_require__(45);

	u = __webpack_require__(19);

	mod.filter('exclude', function() {
	  return function(input, selected) {
	    if (selected == null) {
	      return input;
	    }
	    if (input == null) {
	      return [];
	    }
	    return input.filter(function(item) {
	      return selected.indexOf(item) < 0;
	    });
	  };
	});

	mod.directive("fsMultiselect", [
	  '$templateCache', function($templateCache) {
	    return {
	      restrict: "A",
	      scope: {
	        items: '=',
	        disabled: '=ngDisabled',
	        freetext: '@',
	        "class": '@'
	      },
	      require: '?ngModel',
	      replace: true,
	      template: function(el, attributes) {
	        var defaultItemTpl, itemTpl;
	        defaultItemTpl = "{{ item }}";
	        itemTpl = el.html() || defaultItemTpl;
	        return $templateCache.get('templates/fs/multiselect.html').replace(/::item-template/g, itemTpl);
	      },
	      controller: [
	        '$scope', '$element', '$attrs', '$filter', function($scope, $element, $attrs, $filter) {
	          if ($attrs.freetext != null) {
	            $scope.dynamicItems = function() {
	              if ($scope.search) {
	                return [$scope.search];
	              } else {
	                return [];
	              }
	            };
	          } else {
	            $scope.dynamicItems = function() {
	              return [];
	            };
	          }
	          $scope.updateDropdownItems = function() {
	            var allItems, excludeFilter, searchFilter;
	            searchFilter = $filter('filter');
	            excludeFilter = $filter('exclude');
	            allItems = ($scope.items || []).concat($scope.dynamicItems());
	            return $scope.dropdownItems = searchFilter(excludeFilter(allItems, $scope.selectedItems), $scope.search);
	          };
	          $scope.selectItem = function(item) {
	            if ((item != null) && u.indexOf($scope.selectedItems, item) === -1) {
	              $scope.selectedItems = $scope.selectedItems.concat([item]);
	            }
	            return $scope.search = '';
	          };
	          $scope.unselectItem = function(item) {
	            var index;
	            index = u.indexOf($scope.selectedItems, item);
	            if (index > -1) {
	              return $scope.selectedItems.splice(index, 1);
	            }
	          };
	          $scope.onBlur = function() {
	            $scope.active = false;
	            return $scope.search = '';
	          };
	          $scope.onEnter = function() {
	            return $scope.selectItem($scope.dropdownItems.length > 0 ? $scope.listInterface.selectedItem : null);
	          };
	          $scope.listInterface = {
	            onSelect: function(selectedItem) {
	              return $scope.selectItem(selectedItem);
	            },
	            move: function() {
	              return console.log("not-implemented listInterface.move() function");
	            }
	          };
	          $scope.dropdownItems = [];
	          $scope.active = false;
	          $scope.$watchCollection('selectedItems', function() {
	            return $scope.updateDropdownItems();
	          });
	          $scope.$watchCollection('items', function() {
	            return $scope.updateDropdownItems();
	          });
	          $scope.$watch('search', function() {
	            return $scope.updateDropdownItems();
	          });
	          return $scope.updateDropdownItems();
	        }
	      ],
	      link: function($scope, element, attrs, ngModelCtrl, transcludeFn) {
	        var setViewValue;
	        if (ngModelCtrl) {
	          setViewValue = function(newValue, oldValue) {
	            if (!angular.equals(newValue, oldValue)) {
	              return ngModelCtrl.$setViewValue(newValue);
	            }
	          };
	          $scope.$watch('selectedItems', setViewValue, true);
	          return ngModelCtrl.$render = function() {
	            return $scope.selectedItems = ngModelCtrl.$modelValue || [];
	          };
	        }
	      }
	    };
	  }
	]);


/***/ },
/* 10 */
/***/ function(module, exports, __webpack_require__) {

	var IDEAL_REX, dynamicItems, mkTimeInput, mkTimeItems, mod, si, u, validInput;

	mod = __webpack_require__(18);

	__webpack_require__(46);

	u = __webpack_require__(19);

	si = __webpack_require__(20);

	IDEAL_REX = /^([0-1][0-9]|2[0-3]):([0-5][0-9])$/;

	validInput = function(v) {
	  if (v === '') {
	    return true;
	  }
	  return IDEAL_REX.test(v);
	};

	mkTimeInput = function(el, cb, ddcb) {
	  var items;
	  items = mkTimeItems();
	  el.on('blur', function(e) {
	    var fixed, v;
	    v = el.val();
	    fixed = si.timeLastFix(v);
	    if (fixed !== v && validInput(fixed)) {
	      el.val(fixed);
	      return cb(fixed);
	    } else if (!validInput(v)) {
	      el.val(null);
	      return cb(null);
	    }
	  });
	  el.on('keydown', function(e) {
	    var v;
	    v = el.val();
	    if (/:$/.test(v)) {
	      if (e.which === 8) {
	        return el.val(v.substring(0, v.length - 1));
	      }
	    }
	  });
	  el.on('input', function(e) {
	    var v;
	    v = si.timeInput(el.val());
	    el.val(v);
	    if (v === '') {
	      cb(null, items);
	      ddcb(items);
	    }
	    if (validInput(v)) {
	      cb(v);
	    }
	    return ddcb(items.filter(function(x) {
	      return x.indexOf(v) === 0;
	    }).concat(dynamicItems(v, items)));
	  });
	  return function(v) {
	    return el.val(v);
	  };
	};

	mkTimeItems = function() {
	  var h, hours, items, m, minutes, num, zh, _i, _j, _len, _len1;
	  hours = (function() {
	    var _i, _results;
	    _results = [];
	    for (num = _i = 0; _i <= 23; num = ++_i) {
	      _results.push(num);
	    }
	    return _results;
	  })();
	  minutes = ['00', '15', '30', '45'];
	  items = [];
	  for (_i = 0, _len = hours.length; _i < _len; _i++) {
	    h = hours[_i];
	    zh = h < 10 ? "0" + h : h;
	    for (_j = 0, _len1 = minutes.length; _j < _len1; _j++) {
	      m = minutes[_j];
	      items.push("" + zh + ":" + m);
	    }
	  }
	  return items;
	};

	dynamicItems = function(v, items) {
	  if (v && v.length === 5 && u.indexOf(items, v) === -1) {
	    return [v];
	  } else {
	    return [];
	  }
	};

	mod.directive("fsTime", [
	  '$filter', '$timeout', function($filter, $timeout) {
	    return {
	      restrict: "A",
	      scope: {
	        disabled: '=ngDisabled',
	        "class": '@'
	      },
	      require: '?ngModel',
	      replace: true,
	      templateUrl: 'templates/fs/time.html',
	      link: function(scope, element, attrs, ngModelCtrl) {
	        var dropDownUpdate, scopeUpdate, updateInput, watchFn;
	        scope.dropdownItems = mkTimeItems();
	        dropDownUpdate = function(items) {
	          return scope.$apply(function() {
	            return scope.dropdownItems = items;
	          });
	        };
	        scopeUpdate = function(v) {
	          return scope.$apply(function() {
	            return scope.value = v;
	          });
	        };
	        updateInput = mkTimeInput(element.find('input'), scopeUpdate, dropDownUpdate);
	        scope.$watch('value', function(q) {
	          return updateInput(scope.value);
	        });
	        scope.onBlur = function() {
	          return $timeout((function() {
	            return scope.active = false;
	          }), 0, true);
	        };
	        scope.onEnter = function() {
	          scope.select(scope.listInterface.selectedItem);
	          scope.active = false;
	          return false;
	        };
	        scope.move = function(d) {
	          return scope.listInterface.move && scope.listInterface.move(d);
	        };
	        scope.select = function(value) {
	          scope.value = value;
	          return scope.active = false;
	        };
	        scope.listInterface = {
	          onSelect: scope.select,
	          move: function() {
	            return console.log("not-implemented listInterface.move() function");
	          }
	        };
	        if (ngModelCtrl) {
	          watchFn = function(newValue, oldValue) {
	            if (!angular.equals(newValue, oldValue)) {
	              return ngModelCtrl.$setViewValue(newValue);
	            }
	          };
	          scope.$watch('value', watchFn);
	          return ngModelCtrl.$render = function() {
	            return scope.value = ngModelCtrl.$viewValue;
	          };
	        }
	      }
	    };
	  }
	]);


/***/ },
/* 11 */
/***/ function(module, exports, __webpack_require__) {

	var mod;

	mod = __webpack_require__(18);

	mod.factory("dateParserHelpers", [
	  function() {
	    var cache;
	    cache = {};
	    return {
	      getInteger: function(string, startPoint, minLength, maxLength) {
	        var match, matcher, val;
	        val = string.substring(startPoint);
	        matcher = cache[minLength + "_" + maxLength];
	        if (!matcher) {
	          matcher = new RegExp("^(\\d{" + minLength + "," + maxLength + "})");
	          cache[minLength + "_" + maxLength] = matcher;
	        }
	        match = matcher.exec(val);
	        if (match) {
	          return match[1];
	        }
	        return null;
	      }
	    };
	  }
	]).factory("$dateParser", [
	  "$locale", "dateParserHelpers", function($locale, dateParserHelpers) {
	    var datetimeFormats, dayNames, monthNames;
	    datetimeFormats = $locale.DATETIME_FORMATS;
	    monthNames = datetimeFormats.MONTH.concat(datetimeFormats.SHORTMONTH);
	    dayNames = datetimeFormats.DAY.concat(datetimeFormats.SHORTDAY);
	    return function(val, format) {
	      var ampm, date, day_name, e, format_token, hh, i, i_format, i_val, j, localDate, maxLength, minLength, mm, month, month_name, now, parsedZ, ss, sss, token, tzStr, year, z, _i_format;
	      if (angular.isDate(val)) {
	        return val;
	      }
	      try {
	        val = val + "";
	        format = format + "";
	        if (!format.length) {
	          return new Date(val);
	        }
	        if (datetimeFormats[format]) {
	          format = datetimeFormats[format];
	        }
	        now = new Date();
	        i_val = 0;
	        i_format = 0;
	        format_token = "";
	        year = now.getFullYear();
	        month = now.getMonth() + 1;
	        date = now.getDate();
	        hh = 0;
	        mm = 0;
	        ss = 0;
	        sss = 0;
	        ampm = "am";
	        z = 0;
	        parsedZ = false;
	        while (i_format < format.length) {
	          format_token = format.charAt(i_format);
	          token = "";
	          if (format.charAt(i_format) === "'") {
	            _i_format = i_format;
	            while ((format.charAt(++i_format) !== "'") && (i_format < format.length)) {
	              token += format.charAt(i_format);
	            }
	            if (val.substring(i_val, i_val + token.length) !== token) {
	              throw "Pattern value mismatch";
	            }
	            i_val += token.length;
	            i_format++;
	            continue;
	          }
	          while ((format.charAt(i_format) === format_token) && (i_format < format.length)) {
	            token += format.charAt(i_format++);
	          }
	          if (token === "yyyy" || token === "yy" || token === "y") {
	            minLength = void 0;
	            maxLength = void 0;
	            if (token === "yyyy") {
	              minLength = 4;
	              maxLength = 4;
	            }
	            if (token === "yy") {
	              minLength = 2;
	              maxLength = 2;
	            }
	            if (token === "y") {
	              minLength = 2;
	              maxLength = 4;
	            }
	            year = dateParserHelpers.getInteger(val, i_val, minLength, maxLength);
	            if (year === null) {
	              throw "Invalid year";
	            }
	            i_val += year.length;
	            if (year.length === 2) {
	              if (year > 70) {
	                year = 1900 + (year - 0);
	              } else {
	                year = 2000 + (year - 0);
	              }
	            }
	          } else if (token === "MMMM" || token === "MMM") {
	            month = 0;
	            i = 0;
	            while (i < monthNames.length) {
	              month_name = monthNames[i];
	              if (val.substring(i_val, i_val + month_name.length).toLowerCase() === month_name.toLowerCase()) {
	                month = i + 1;
	                if (month > 12) {
	                  month -= 12;
	                }
	                i_val += month_name.length;
	                break;
	              }
	              i++;
	            }
	            if ((month < 1) || (month > 12)) {
	              throw "Invalid month";
	            }
	          } else if (token === "EEEE" || token === "EEE") {
	            j = 0;
	            while (j < dayNames.length) {
	              day_name = dayNames[j];
	              if (val.substring(i_val, i_val + day_name.length).toLowerCase() === day_name.toLowerCase()) {
	                i_val += day_name.length;
	                break;
	              }
	              j++;
	            }
	          } else if (token === "MM" || token === "M") {
	            month = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (month === null || (month < 1) || (month > 12)) {
	              throw "Invalid month";
	            }
	            i_val += month.length;
	          } else if (token === "dd" || token === "d") {
	            date = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (date === null || (date < 1) || (date > 31)) {
	              throw "Invalid date";
	            }
	            i_val += date.length;
	          } else if (token === "HH" || token === "H") {
	            hh = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (hh === null || (hh < 0) || (hh > 23)) {
	              throw "Invalid hours";
	            }
	            i_val += hh.length;
	          } else if (token === "hh" || token === "h") {
	            hh = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (hh === null || (hh < 1) || (hh > 12)) {
	              throw "Invalid hours";
	            }
	            i_val += hh.length;
	          } else if (token === "mm" || token === "m") {
	            mm = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (mm === null || (mm < 0) || (mm > 59)) {
	              throw "Invalid minutes";
	            }
	            i_val += mm.length;
	          } else if (token === "ss" || token === "s") {
	            ss = dateParserHelpers.getInteger(val, i_val, token.length, 2);
	            if (ss === null || (ss < 0) || (ss > 59)) {
	              throw "Invalid seconds";
	            }
	            i_val += ss.length;
	          } else if (token === "sss") {
	            sss = dateParserHelpers.getInteger(val, i_val, 3, 3);
	            if (sss === null || (sss < 0) || (sss > 999)) {
	              throw "Invalid milliseconds";
	            }
	            i_val += 3;
	          } else if (token === "a") {
	            if (val.substring(i_val, i_val + 2).toLowerCase() === "am") {
	              ampm = "AM";
	            } else if (val.substring(i_val, i_val + 2).toLowerCase() === "pm") {
	              ampm = "PM";
	            } else {
	              throw "Invalid AM/PM";
	            }
	            i_val += 2;
	          } else if (token === "Z") {
	            parsedZ = true;
	            if (val[i_val] === "Z") {
	              z = 0;
	              i_val += 1;
	            } else {
	              if (val[i_val + 3] === ":") {
	                tzStr = val.substring(i_val, i_val + 6);
	                z = (parseInt(tzStr.substr(0, 3), 10) * 60) + parseInt(tzStr.substr(4, 2), 10);
	                i_val += 6;
	              } else {
	                tzStr = val.substring(i_val, i_val + 5);
	                z = (parseInt(tzStr.substr(0, 3), 10) * 60) + parseInt(tzStr.substr(3, 2), 10);
	                i_val += 5;
	              }
	            }
	            if (z > 720 || z < -720) {
	              throw "Invalid timezone";
	            }
	          } else {
	            if (val.substring(i_val, i_val + token.length) !== token) {
	              throw "Pattern value mismatch";
	            } else {
	              i_val += token.length;
	            }
	          }
	        }
	        if (i_val !== val.length) {
	          throw "Pattern value mismatch";
	        }
	        year = parseInt(year, 10);
	        month = parseInt(month, 10);
	        date = parseInt(date, 10);
	        hh = parseInt(hh, 10);
	        mm = parseInt(mm, 10);
	        ss = parseInt(ss, 10);
	        sss = parseInt(sss, 10);
	        if (month === 2) {
	          if (((year % 4 === 0) && (year % 100 !== 0)) || (year % 400 === 0)) {
	            if (date > 29) {
	              throw "Invalid date";
	            }
	          } else {
	            if (date > 28) {
	              throw "Invalid date";
	            }
	          }
	        }
	        if ((month === 4) || (month === 6) || (month === 9) || (month === 11)) {
	          if (date > 30) {
	            throw "Invalid date";
	          }
	        }
	        if (hh < 12 && ampm === "PM") {
	          hh += 12;
	        } else {
	          if (hh > 11 && ampm === "AM") {
	            hh -= 12;
	          }
	        }
	        localDate = new Date(year, month - 1, date, hh, mm, ss, sss);
	        if (parsedZ) {
	          return new Date(localDate.getTime() - (z + localDate.getTimezoneOffset()) * 60000);
	        }
	        return localDate;
	      } catch (_error) {
	        e = _error;
	        return undefined;
	      }
	    };
	  }
	]);


/***/ },
/* 12 */
/***/ function(module, exports, __webpack_require__) {

	var mod;

	mod = __webpack_require__(18);

	__webpack_require__(11);

	mod.directive('fsDateFormat', [
	  '$filter', '$dateParser', function($filter, $dateParser) {
	    return {
	      restrict: 'A',
	      require: 'ngModel',
	      link: function(scope, element, attrs, ngModel) {
	        var dateFilter, format;
	        format = attrs.fsDateFormat || 'shortDate';
	        dateFilter = $filter('date');
	        ngModel.$formatters.push(function(value) {
	          return dateFilter(value, format);
	        });
	        return ngModel.$parsers.unshift(function(value) {
	          var result;
	          if (!value) {
	            return null;
	          }
	          if (value === '') {
	            return null;
	          }
	          result = $dateParser(value, format);
	          if (result) {
	            return result;
	          } else {
	            return null;
	          }
	        });
	      }
	    };
	  }
	]);


/***/ },
/* 13 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(47);

	u = __webpack_require__(19);

	mod.directive('fsDate', function() {
	  return {
	    restrict: 'EA',
	    require: '?ngModel',
	    scope: {
	      "class": '@',
	      disabled: '=ngDisabled',
	      placeholder: '@',
	      format: '@'
	    },
	    templateUrl: 'templates/fs/date.html',
	    replace: true,
	    link: function($scope, element, attrs, ngModel) {
	      $scope.selectedDate = {};
	      if (ngModel) {
	        ngModel.$render = function() {
	          return $scope.selectedDate.date = ngModel.$modelValue;
	        };
	        $scope.$watch('selectedDate.date', function(newDate, oldDate) {
	          var updatedDate;
	          updatedDate = u.updateDate(newDate, oldDate);
	          if ((updatedDate != null ? updatedDate.getTime() : void 0) !== (oldDate != null ? oldDate.getTime() : void 0)) {
	            return ngModel.$setViewValue(updatedDate);
	          }
	        });
	      }
	      return $scope.close = function() {
	        return $scope.active = false;
	      };
	    }
	  };
	});


/***/ },
/* 14 */
/***/ function(module, exports, __webpack_require__) {

	var mod, shiftWeekDays, u;

	mod = __webpack_require__(18);

	__webpack_require__(33);

	__webpack_require__(48);

	u = __webpack_require__(19);

	shiftWeekDays = function(weekDays, firstDayOfWeek) {
	  var weekDaysHead;
	  weekDaysHead = weekDays.slice(firstDayOfWeek, weekDays.length);
	  return weekDaysHead.concat(weekDays.slice(0, firstDayOfWeek));
	};

	mod.directive('fsCalendar', [
	  '$locale', function($locale) {
	    return {
	      restrict: 'EA',
	      templateUrl: 'templates/fs/calendar.html',
	      replace: true,
	      require: '?ngModel',
	      scope: {
	        onSelect: '&'
	      },
	      controller: [
	        '$scope', '$attrs', function($scope, $attrs) {
	          var addDays, currentTime, i, updateSelectionRanges;
	          $scope.selectionMode = 'day';
	          $scope.months = $locale.DATETIME_FORMATS.SHORTMONTH;
	          currentTime = new Date();
	          $scope.currentDate = new Date(currentTime.getFullYear(), currentTime.getMonth(), currentTime.getDate());
	          $scope.selectedYear = $scope.currentDate.getFullYear();
	          $scope.selectedMonth = $scope.months[$scope.currentDate.getMonth()];
	          $scope.monthGroups = (function() {
	            var _i, _results;
	            _results = [];
	            for (i = _i = 0; _i <= 2; i = ++_i) {
	              _results.push($scope.months.slice(i * 4, i * 4 + 4));
	            }
	            return _results;
	          })();
	          $scope.prevMonth = function() {
	            var month;
	            month = u.indexOf($scope.months, $scope.selectedMonth) - 1;
	            if (month < 0) {
	              month = $scope.months.length - 1;
	              $scope.selectedYear--;
	            }
	            return $scope.selectedMonth = $scope.months[month];
	          };
	          $scope.nextMonth = function() {
	            var month;
	            month = u.indexOf($scope.months, $scope.selectedMonth) + 1;
	            if (month >= $scope.months.length) {
	              month = 0;
	              $scope.selectedYear++;
	            }
	            return $scope.selectedMonth = $scope.months[month];
	          };
	          $scope.prevYear = function() {
	            return $scope.selectedYear--;
	          };
	          $scope.nextYear = function() {
	            return $scope.selectedYear++;
	          };
	          $scope.prevYearRange = function() {
	            var rangeSize, _i, _ref, _ref1, _results;
	            rangeSize = $scope.years.length;
	            return $scope.years = (function() {
	              _results = [];
	              for (var _i = _ref = $scope.years[0] - rangeSize, _ref1 = $scope.years[$scope.years.length - 1] - rangeSize; _ref <= _ref1 ? _i <= _ref1 : _i >= _ref1; _ref <= _ref1 ? _i++ : _i--){ _results.push(_i); }
	              return _results;
	            }).apply(this);
	          };
	          $scope.nextYearRange = function() {
	            var rangeSize, _i, _ref, _ref1, _results;
	            rangeSize = $scope.years.length;
	            return $scope.years = (function() {
	              _results = [];
	              for (var _i = _ref = $scope.years[0] + rangeSize, _ref1 = $scope.years[$scope.years.length - 1] + rangeSize; _ref <= _ref1 ? _i <= _ref1 : _i >= _ref1; _ref <= _ref1 ? _i++ : _i--){ _results.push(_i); }
	              return _results;
	            }).apply(this);
	          };
	          $scope.switchSelectionMode = function() {
	            return $scope.selectionMode = (function() {
	              switch ($scope.selectionMode) {
	                case 'day':
	                  return 'month';
	                case 'month':
	                  return 'year';
	                default:
	                  return 'day';
	              }
	            })();
	          };
	          $scope.isDayInSelectedMonth = function(day) {
	            return day.getFullYear() === $scope.selectedYear && $scope.months[day.getMonth()] === $scope.selectedMonth;
	          };
	          $scope.isCurrentDate = function(day) {
	            var _ref;
	            return day.getTime() === ((_ref = $scope.currentDate) != null ? _ref.getTime() : void 0);
	          };
	          $scope.isSelectedDate = function(day) {
	            var _ref;
	            return day.getTime() === ((_ref = $scope.selectedDate) != null ? _ref.getTime() : void 0);
	          };
	          addDays = function(date, days) {
	            return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
	          };
	          updateSelectionRanges = function() {
	            var day, dayOffset, firstDayOfMonth, firstDayOfWeek, monthIndex, week, _i, _ref, _ref1, _results;
	            monthIndex = u.indexOf($scope.months, $scope.selectedMonth);
	            firstDayOfMonth = new Date($scope.selectedYear, monthIndex);
	            dayOffset = $scope.firstDayOfWeek - firstDayOfMonth.getDay();
	            if (dayOffset > 0) {
	              dayOffset -= 7;
	            }
	            firstDayOfWeek = addDays(firstDayOfMonth, dayOffset);
	            $scope.weeks = (function() {
	              var _i, _results;
	              _results = [];
	              for (week = _i = 0; _i <= 5; week = ++_i) {
	                _results.push((function() {
	                  var _j, _results1;
	                  _results1 = [];
	                  for (day = _j = 0; _j <= 6; day = ++_j) {
	                    _results1.push(addDays(firstDayOfWeek, 7 * week + day));
	                  }
	                  return _results1;
	                })());
	              }
	              return _results;
	            })();
	            return $scope.years = (function() {
	              _results = [];
	              for (var _i = _ref = $scope.selectedYear - 5, _ref1 = $scope.selectedYear + 6; _ref <= _ref1 ? _i <= _ref1 : _i >= _ref1; _ref <= _ref1 ? _i++ : _i--){ _results.push(_i); }
	              return _results;
	            }).apply(this);
	          };
	          $scope.$watch('selectedDate', function() {
	            if ($scope.selectedDate != null) {
	              $scope.selectedYear = $scope.selectedDate.getFullYear();
	              return $scope.selectedMonth = $scope.months[$scope.selectedDate.getMonth()];
	            }
	          });
	          $scope.$watch('selectedMonth', updateSelectionRanges);
	          $scope.$watch('selectedYear', updateSelectionRanges);
	          $scope.$watch('years', function() {
	            return $scope.yearGroups = (function() {
	              var _i, _results;
	              _results = [];
	              for (i = _i = 0; _i <= 3; i = ++_i) {
	                _results.push($scope.years.slice(i * 4, i * 4 + 4));
	              }
	              return _results;
	            })();
	          });
	          $scope.firstDayOfWeek = parseInt($attrs.firstDayOfWeek || 0);
	          return $scope.weekDays = shiftWeekDays($locale.DATETIME_FORMATS.SHORTDAY, $scope.firstDayOfWeek);
	        }
	      ],
	      link: function(scope, element, attrs, ngModel) {
	        scope.isSameYear = function() {
	          var _ref;
	          return ((_ref = u.parseDate(ngModel.$modelValue)) != null ? _ref.getFullYear() : void 0) === scope.selectedYear;
	        };
	        scope.selectDay = function(day) {
	          scope.selectedDate = day;
	          ngModel.$setViewValue(day);
	          return scope.onSelect();
	        };
	        ngModel.$render = function() {
	          return scope.selectedDate = u.parseDate(ngModel.$modelValue);
	        };
	        scope.selectMonth = function(monthName) {
	          scope.selectionMode = 'day';
	          scope.selectedDate = void 0;
	          return scope.selectedMonth = monthName;
	        };
	        return scope.selectYear = function(year) {
	          scope.selectionMode = 'month';
	          scope.selectedDate = void 0;
	          return scope.selectedYear = year;
	        };
	      }
	    };
	  }
	]);


/***/ },
/* 15 */
/***/ function(module, exports, __webpack_require__) {

	var mod, u;

	mod = __webpack_require__(18);

	__webpack_require__(49);

	__webpack_require__(27);

	__webpack_require__(35);

	u = __webpack_require__(19);

	mod.directive("fsDatetime", function() {
	  return {
	    restrict: "A",
	    scope: {
	      disabled: '=ngDisabled',
	      "class": '@'
	    },
	    require: '?ngModel',
	    replace: true,
	    templateUrl: 'templates/fs/datetime.html',
	    controller: [
	      '$scope', function($scope) {
	        return $scope.clearDate = function() {
	          $scope.time = null;
	          $scope.date = null;
	          return $scope.value = null;
	        };
	      }
	    ],
	    link: function(scope, element, attrs, ngModelCtrl, transcludeFn) {
	      if (ngModelCtrl) {
	        scope.value = null;
	        scope.$watch('time', function(newValue, oldValue) {
	          var hours, minutes, parts;
	          if (!angular.equals(newValue, oldValue)) {
	            if (newValue) {
	              parts = newValue.split(':');
	              minutes = parseInt(parts[1]) || 0;
	              hours = parseInt(parts[0]) || 0;
	              scope.value || (scope.value = new Date());
	              scope.value = angular.copy(scope.value);
	              scope.value.setHours(hours);
	              scope.value.setMinutes(minutes);
	              scope.value.setSeconds(0);
	              return scope.value.setMilliseconds(0);
	            }
	          }
	        });
	        scope.$watch('date', function(newValue, oldValue) {
	          if (!angular.equals(newValue, oldValue)) {
	            if (newValue) {
	              scope.value || (scope.value = new Date());
	              scope.value = angular.copy(scope.value);
	              scope.value.setDate(newValue.getDate());
	              scope.value.setMonth(newValue.getMonth());
	              return scope.value.setFullYear(newValue.getFullYear());
	            }
	          }
	        });
	        scope.$watch('value', function(newValue, oldValue) {
	          if (!angular.equals(newValue, oldValue)) {
	            return ngModelCtrl.$setViewValue(scope.value);
	          }
	        });
	        return ngModelCtrl.$render = function() {
	          scope.date = scope.value = ngModelCtrl.$viewValue;
	          return scope.time = ngModelCtrl.$viewValue ? u.toTimeStr({
	            hours: ngModelCtrl.$viewValue.getHours(),
	            minutes: ngModelCtrl.$viewValue.getMinutes()
	          }) : null;
	        };
	      }
	    }
	  };
	});


/***/ },
/* 16 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 17 */,
/* 18 */
/***/ function(module, exports, __webpack_require__) {

	module.exports = angular.module('formstamp', ['ng']);


/***/ },
/* 19 */
/***/ function(module, exports, __webpack_require__) {

	var getComputedStyleFor, innerHeightOf, uid;

	exports.filter = function(x, xs, valueAttr) {
	  if (x) {
	    return xs.filter(function(i) {
	      var item;
	      item = valueAttr ? i[valueAttr] : i;
	      return comp(item, x);
	    });
	  } else {
	    return xs;
	  }
	};

	exports.indexOf = function(array, elem) {
	  var index, _i, _ref;
	  for (index = _i = 0, _ref = array.length - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; index = 0 <= _ref ? ++_i : --_i) {
	    if (angular.equals(array[index], elem)) {
	      return index;
	    }
	  }
	  return -1;
	};

	getComputedStyleFor = function(elem, prop) {
	  return parseInt(window.getComputedStyle(elem, null).getPropertyValue(prop));
	};

	innerHeightOf = function(elem) {
	  return elem.clientHeight - getComputedStyleFor(elem, 'padding-top') - getComputedStyleFor(elem, 'padding-bottom');
	};

	exports.scrollToTarget = function(container, target) {
	  var item, viewport;
	  if (!(container && target)) {
	    return;
	  }
	  viewport = {
	    top: container.scrollTop,
	    bottom: container.scrollTop + innerHeightOf(container)
	  };
	  item = {
	    top: target.offsetTop,
	    bottom: target.offsetTop + target.offsetHeight
	  };
	  if (item.bottom > viewport.bottom) {
	    return container.scrollTop += item.bottom - viewport.bottom;
	  } else if (item.top < viewport.top) {
	    return container.scrollTop -= viewport.top - item.top;
	  }
	};

	exports.addValidations = function(attrs, ctrl) {
	  var match, maxLengthValidator, maxlength, minLengthValidator, minlength, pattern, patternValidator, validate, validateRegex;
	  validate = function(ctrl, validatorName, validity, value) {
	    ctrl.$setValidity(validatorName, validity);
	    if (validity) {
	      return value;
	    } else {
	      return void 0;
	    }
	  };
	  if (attrs.ngMinlength) {
	    minlength = parseInt(attrs.ngMinlength);
	    minLengthValidator = function(value) {
	      return validate(ctrl, 'minlength', ctrl.$isEmpty(value) || value.length >= minlength, value);
	    };
	    ctrl.$formatters.push(minLengthValidator);
	    ctrl.$parsers.push(minLengthValidator);
	  }
	  if (attrs.ngMaxlength) {
	    maxlength = parseInt(attrs.ngMaxlength);
	    maxLengthValidator = function(value) {
	      return validate(ctrl, 'maxlength', ctrl.$isEmpty(value) || value.length <= maxlength, value);
	    };
	    ctrl.$formatters.push(maxLengthValidator);
	    ctrl.$parsers.push(maxLengthValidator);
	  }
	  if (attrs.ngPattern) {
	    pattern = attrs.ngPattern;
	    validateRegex = function(regexp, value) {
	      return validate(ctrl, 'pattern', ctrl.$isEmpty(value) || regexp.test(value), value);
	    };
	    match = pattern.match(/^\/(.*)\/([gim]*)$/);
	    if (match) {
	      pattern = new RegExp(match[1], match[2]);
	      patternValidator = function(value) {
	        return validateRegex(pattern, value);
	      };
	    } else {
	      patternValidator = function(value) {
	        var patternObj;
	        patternObj = scope.$eval(pattern);
	        if (!patternObj || !patternObj.test) {
	          throw minErr('ngPattern')('noregexp', 'Expected {0} to be a RegExp but was {1}. Element: {2}', pattern, patternObj, startingTag(element));
	        }
	        return validateRegex(patternObj, value);
	      };
	    }
	    ctrl.$formatters.push(patternValidator);
	    return ctrl.$parsers.push(patternValidator);
	  }
	};

	exports.updateTime = function(date, time) {
	  if (date != null) {
	    date.setHours(time.getHours());
	    date.setMinutes(time.getMinutes());
	  }
	  return date;
	};

	exports.updateDate = function(date, newDate) {
	  switch (false) {
	    case !(date == null):
	      return newDate;
	    case !(newDate == null):
	      return date;
	    default:
	      date.setHours(newDate.getHours());
	      date.setMinutes(newDate.getMinutes());
	      date.setSeconds(newDate.getSeconds());
	      return date;
	  }
	};

	exports.parseDate = function(dateString) {
	  var parsedDate, time;
	  time = Date.parse(dateString);
	  if (!isNaN(time)) {
	    parsedDate = new Date(time);
	    return new Date(parsedDate.getFullYear(), parsedDate.getMonth(), parsedDate.getDate());
	  }
	};

	uid = ['0', '0', '0'];

	exports.nextUid = function() {
	  var digit, index;
	  index = uid.length;
	  digit;
	  while (index) {
	    index -= 1;
	    digit = uid[index].charCodeAt(0);
	    if (digit === 57) {
	      uid[index] = 'A';
	      return uid.join('');
	    }
	    if (digit === 90) {
	      uid[index] = '0';
	    } else {
	      uid[index] = String.fromCharCode(digit + 1);
	      return uid.join('');
	    }
	  }
	  uid.unshift('0');
	  return uid.join('');
	};

	exports.toTimeStr = function(time) {
	  var h, m, _ref, _ref1;
	  if (!((time != null) && (time.hours != null) && (time.minutes != null))) {
	    return '';
	  }
	  h = (_ref = time.hours) != null ? _ref.toString() : void 0;
	  if ((h != null ? h.length : void 0) < 2) {
	    h = "0" + h;
	  }
	  m = (_ref1 = time.minutes) != null ? _ref1.toString() : void 0;
	  if ((m != null ? m.length : void 0) < 2) {
	    m = "0" + m;
	  }
	  return "" + h + ":" + m;
	};


/***/ },
/* 20 */
/***/ function(module, exports, __webpack_require__) {

	var timeFixChain, timeInput, timeInputChain, timeLastFix;

	timeInputChain = {
	  1: [[/^([^0-9])$/, ''], [/^([0-2])$/, '$1'], [/^([3-9])$/, '0$1:']],
	  2: [[/^([0-1][0-9]|2[0-3])$/, '$1:'], [/^2[^0-3]$/, '2']]
	};

	timeInput = function(v) {
	  var chain, exp, rep, _i, _len, _ref;
	  chain = timeInputChain[v.length];
	  if (chain) {
	    for (_i = 0, _len = chain.length; _i < _len; _i++) {
	      _ref = chain[_i], exp = _ref[0], rep = _ref[1];
	      v = v.replace(exp, rep);
	    }
	  }
	  return v;
	};

	timeFixChain = [[/^([0-1][0-9]|2[0-3])$/, '$1:00'], [/^([0-1][0-9]|2[0-3]):$/, '$1:00'], [/^([0-1][0-9]|2[0-3]):([0-9])$/, '$1:0$2']];

	timeLastFix = function(v) {
	  var exp, rep, _i, _len, _ref;
	  for (_i = 0, _len = timeFixChain.length; _i < _len; _i++) {
	    _ref = timeFixChain[_i], exp = _ref[0], rep = _ref[1];
	    v = v.replace(exp, rep);
	  }
	  return v;
	};

	exports.timeInput = timeInput;

	exports.timeLastFix = timeLastFix;


/***/ },
/* 21 */,
/* 22 */,
/* 23 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 24 */,
/* 25 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 26 */,
/* 27 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 28 */,
/* 29 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 30 */,
/* 31 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 32 */,
/* 33 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 34 */,
/* 35 */
/***/ function(module, exports, __webpack_require__) {

	// removed by extract-text-webpack-plugin

/***/ },
/* 36 */,
/* 37 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-widget-root fs-radio fs-racheck\" ng-class=\"{disabled: disabled, enabled: !disabled}\"> <div class=\"fs-radio-item\" ng-repeat=\"item in items\"> <input fs-null-form type=\"radio\" ng-model=\"$parent.selectedItem\" name=\"::name\" ng-value=\"item\" ng-disabled=\"disabled\" id=\"::name_{{$index}}\"/> <label for=\"::name_{{$index}}\"> <span class=\"fs-radio-btn\"><span></span></span>\n::itemTpl </label> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/metaRadio.html", v1)}]);
	module.exports=v1;

/***/ },
/* 38 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"dropdown open fs-list\"> <ul class=\"dropdown-menu\" role=\"menu\"> <li ng-repeat=\"item in items\" ng-class=\"{true: 'active'}[$index == highlightIndex]\"> <a ng-click=\"highlightItem(item)\" href=\"javascript:void(0)\" tabindex=\"-1\"> <span>::itemTpl</span> </a> </li> </ul> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/list.html", v1)}]);
	module.exports=v1;

/***/ },
/* 39 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-racheck fs-checkbox\" ng-class=\"{disabled: disabled, enabled: !disabled}\"> <div ng-repeat=\"item in items\"> <div class=\"fs-racheck-item\" href=\"javascript:void(0)\" ng-disabled=\"disabled\" ng-click=\"toggle(item)\" fs-space=\"toggle(item)\"> <span class=\"fs-check-outer\"><span ng-show=\"isSelected(item)\" class=\"fs-check-inner\"></span></span>\n::itemTpl </div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/metaCheckbox.html", v1)}]);
	module.exports=v1;

/***/ },
/* 40 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"form-group\" ng-class=\"{&quot;has-error&quot;: object.$errors[field].length > 0}\"> <label for=\"{{ objectName }}[{{ field }}]\" class=\"col-sm-2 control-label\">Name</label> <div class=\"col-sm-10\"> <div items=\"items\" invalid=\"object.$errors[field]\" name=\"{{ objectName }}[{{ field }}]\" ng-model=\"object[field]\"></div> <div> <p ng-repeat=\"error in object.$errors[field]\" class=\"text-danger\">{{ error }}</p> </div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/field.html", v1)}]);
	module.exports=v1;

/***/ },
/* 41 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"form-group\" ng-class=\"{'has-error': (::formName.::name.$dirty && ::formName.::name.$invalid)}\"> <label class=\"col-sm-2 control-label\">::label</label> <div class=\"col-sm-10\"> ::content <div fs-errors model=\"::formName.::name\"></div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/metaInput.html", v1)}]);
	module.exports=v1;

/***/ },
/* 42 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"form-group\"> <label class=\"col-sm-2 control-label\">::label</label> <div class=\"col-sm-10\"> ::content </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/metaRow.html", v1)}]);
	module.exports=v1;

/***/ },
/* 43 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<ul class=\"text-danger fs-errors\" ng-show=\"model.$dirty && messages && messages.length > 0\"> <li ng-repeat=\"msg in messages\">{{ msg }}</li> </ul>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/errors.html", v1)}]);
	module.exports=v1;

/***/ },
/* 44 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-select fs-widget-root\" ng-class=\"'async-state-' + asyncState\"> <div ng-hide=\"active\" class=\"fs-select-sel\" ng-class=\"{'btn-group': item}\"> <a class=\"btn btn-default activate-button\" ng-class=\"{&quot;btn-danger&quot;: invalid}\" href=\"javascript:void(0)\" ng-click=\"active = true\" ng-disabled=\"disabled\"> ::itemTpl &nbsp; </a>\n<button type=\"button\" class=\"btn btn-default fs-close\" aria-hidden=\"true\" ng-show=\"item\" ng-disabled=\"disabled\" ng-click=\"unselectItem()\"></button> </div> <div class=\"open\" ng-show=\"active\"> <input class=\"form-control\" fs-input fs-focus-when=\"active\" fs-blur-when=\"!active\" fs-on-focus=\"active = true\" fs-on-blur=\"onBlur()\" fs-hold-focus fs-down=\"move(1)\" fs-up=\"move(-1)\" fs-pg-up=\"move(-11)\" fs-pg-down=\"move(11)\" fs-enter=\"onEnter($event)\" fs-esc=\"active = false\" type=\"text\" placeholder=\"Search\" ng-model=\"search\" fs-null-form/> <div class=\"spinner\"></div> <div ng-if=\"active && dropdownItems.length > 0\"> <div fs-list items=\"dropdownItems\"> ::itemTpl </div> </div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/metaSelect.html", v1)}]);
	module.exports=v1;

/***/ },
/* 45 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-multiselect fs-widget-root\" ng-class=\"{ &quot;fs-with-selected-items&quot;: selectedItems.length > 0 }\"> <div class=\"fs-multiselect-wrapper\"> <div class=\"fs-multiselect-selected-items\" ng-if=\"selectedItems.length > 0\"> <a ng-repeat=\"item in selectedItems\" class=\"btn\" ng-click=\"unselectItem(item)\" ng-disabled=\"disabled\"> ::item-template\n<span class=\"fs-close\"></span> </a> </div> <input ng-keydown=\"onkeys($event)\" fs-null-form ng-disabled=\"disabled\" fs-input fs-hold-focus fs-on-focus=\"active = true\" fs-on-blur=\"onBlur()\" fs-blur-when=\"!active\" fs-down=\"listInterface.move(1)\" fs-up=\"listInterface.move(-1)\" fs-pgup=\"listInterface.move(-11)\" fs-pgdown=\"listInterface.move(11)\" fs-enter=\"onEnter()\" fs-esc=\"active = false\" class=\"form-control\" type=\"text\" placeholder=\"Select something\" ng-model=\"search\"/> <div ng-if=\"active && dropdownItems.length > 0\" class=\"open\"> <div fs-list items=\"dropdownItems\"> ::item-template </div> </div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/multiselect.html", v1)}]);
	module.exports=v1;

/***/ },
/* 46 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-time fs-widget-root\"> <input fs-null-form fs-input fs-focus-when=\"active\" fs-blur-when=\"!active\" fs-on-focus=\"active = true\" fs-on-blur=\"onBlur()\" fs-hold-focus fs-down=\"move(1)\" fs-up=\"move(-1)\" fs-pg-up=\"move(-11)\" fs-pg-down=\"move(11)\" fs-enter=\"onEnter()\" fs-esc=\"active = false\" class=\"form-control fs-time-role\" ng-disabled=\"disabled\" type=\"text\"/>\n<span class=\"glyphicon glyphicon-time\" ng-click=\"active = !disabled\"></span> <div ng-if=\"!disabled && active\" fs-list items=\"dropdownItems\"> {{item}} </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/time.html", v1)}]);
	module.exports=v1;

/***/ },
/* 47 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-date fs-widget-root\"> <input fs-input fs-focus-when=\"active\" fs-blur-when=\"!active\" fs-on-focus=\"active = true\" fs-on-blur=\"active = false\" fs-hold-focus fs-esc=\"active = false\" type=\"text\" ng-disabled=\"disabled\" class=\"form-control\" ng-model=\"selectedDate.date\" fs-date-format=\"{{format}}\" placeholder=\"{{placeholder}}\" fs-null-form/>\n<span class=\"glyphicon glyphicon-calendar\" ng-click=\"active = !disabled\"></span> <div ng-if=\"!disabled && active\" class=\"open fs-calendar-wrapper\"> <div class=\"dropdown-menu\"> <fs-calendar ng-model=\"selectedDate.date\" on-select=\"close()\"/> </div> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/date.html", v1)}]);
	module.exports=v1;

/***/ },
/* 48 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-calendar\" data-ng-switch=\"selectionMode\"> <div data-ng-switch-when=\"year\"> <div class=\"fs-calendar-header\"> <span class=\"fs-calendar-prev\" data-ng-click=\"prevYearRange()\"></span>\n<span class=\"fs-calendar-title\" data-ng-click=\"switchSelectionMode()\"> {{ years[0] }}-{{ years[years.length-1] }} </span>\n<span class=\"fs-calendar-next\" data-ng-click=\"nextYearRange()\"></span> </div> <table class=\"table-condensed\"> <tr data-ng-repeat=\"yearGroup in yearGroups\"> <td data-ng-repeat=\"year in yearGroup\" data-ng-click=\"selectYear(year)\" data-ng-class=\"{'active': year == selectedYear}\" class=\"year\"> {{ year }} </td> </tr> </table> </div> <div data-ng-switch-when=\"month\"> <div class=\"fs-calendar-header\"> <span class=\"fs-calendar-prev\" data-ng-click=\"prevYear()\"></span>\n<span class=\"fs-calendar-title\" data-ng-click=\"switchSelectionMode()\"> {{ selectedYear }} </span>\n<span class=\"fs-calendar-next\" data-ng-click=\"nextYear()\"></span> </div> <table class=\"table-condensed\"> <tr data-ng-repeat=\"monthGroup in monthGroups\"> <td data-ng-repeat=\"month in monthGroup\" data-ng-click=\"selectMonth(month)\" data-ng-class=\"{'active': month == selectedMonth && isSameYear()}\" class=\"month\"> {{ month }} </td> </tr> </table> </div> <div data-ng-switch-default> <div class=\"fs-calendar-header\"> <span class=\"fs-calendar-prev\" data-ng-click=\"prevMonth()\"></span>\n<span class=\"fs-calendar-title\" data-ng-click=\"switchSelectionMode()\"> {{ selectedMonth + ', ' + selectedYear }} </span>\n<span class=\"fs-calendar-next\" data-ng-click=\"nextMonth()\"></span> </div> <table class=\"table-condensed\"> <thead> <tr> <th data-ng-repeat=\"weekDay in weekDays\"> {{ weekDay }} </th> </tr> </thead> <tbody> <tr data-ng-repeat=\"week in weeks\"> <td data-ng-repeat=\"day in week\" class=\"day\" data-ng-class=\"{'day-in-selected-month': isDayInSelectedMonth(day),\n                       'day-current': isCurrentDate(day),\n                       'active bg-info': isSelectedDate(day)}\" data-ng-click=\"selectDay(day)\"> {{ day.getDate() }} </td> </tr> </tbody> </table> </div> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/calendar.html", v1)}]);
	module.exports=v1;

/***/ },
/* 49 */
/***/ function(module, exports, __webpack_require__) {

	var v1="<div class=\"fs-datetime fs-widget-root\" ng-class=\"{ &quot;fs-with-value&quot;: value }\"> <div fs-date ng-model=\"date\" ng-disabled=\"disabled\" fs-null-form></div> <div fs-time ng-model=\"time\" ng-disabled=\"disabled\" fs-null-form with-date></div> <button type=\"button\" class=\"btn btn-default fs-close\" ng-show=\"value\" ng-disabled=\"disabled\" ng-click=\"clearDate()\"></button> </div>";
	window.angular.module(["ng"]).run(["$templateCache",function(c){c.put("templates/fs/datetime.html", v1)}]);
	module.exports=v1;

/***/ }
/******/ ])
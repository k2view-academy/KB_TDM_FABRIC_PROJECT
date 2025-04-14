/******/ (function(modules) { // webpackBootstrap
/******/ 	// install a JSONP callback for chunk loading
/******/ 	function webpackJsonpCallback(data) {
/******/ 		var chunkIds = data[0];
/******/ 		var moreModules = data[1];
/******/ 		var executeModules = data[2];
/******/
/******/ 		// add "moreModules" to the modules object,
/******/ 		// then flag all "chunkIds" as loaded and fire callback
/******/ 		var moduleId, chunkId, i = 0, resolves = [];
/******/ 		for(;i < chunkIds.length; i++) {
/******/ 			chunkId = chunkIds[i];
/******/ 			if(Object.prototype.hasOwnProperty.call(installedChunks, chunkId) && installedChunks[chunkId]) {
/******/ 				resolves.push(installedChunks[chunkId][0]);
/******/ 			}
/******/ 			installedChunks[chunkId] = 0;
/******/ 		}
/******/ 		for(moduleId in moreModules) {
/******/ 			if(Object.prototype.hasOwnProperty.call(moreModules, moduleId)) {
/******/ 				modules[moduleId] = moreModules[moduleId];
/******/ 			}
/******/ 		}
/******/ 		if(parentJsonpFunction) parentJsonpFunction(data);
/******/
/******/ 		while(resolves.length) {
/******/ 			resolves.shift()();
/******/ 		}
/******/
/******/ 		// add entry modules from loaded chunk to deferred list
/******/ 		deferredModules.push.apply(deferredModules, executeModules || []);
/******/
/******/ 		// run deferred modules when all chunks ready
/******/ 		return checkDeferredModules();
/******/ 	};
/******/ 	function checkDeferredModules() {
/******/ 		var result;
/******/ 		for(var i = 0; i < deferredModules.length; i++) {
/******/ 			var deferredModule = deferredModules[i];
/******/ 			var fulfilled = true;
/******/ 			for(var j = 1; j < deferredModule.length; j++) {
/******/ 				var depId = deferredModule[j];
/******/ 				if(installedChunks[depId] !== 0) fulfilled = false;
/******/ 			}
/******/ 			if(fulfilled) {
/******/ 				deferredModules.splice(i--, 1);
/******/ 				result = __webpack_require__(__webpack_require__.s = deferredModule[0]);
/******/ 			}
/******/ 		}
/******/
/******/ 		return result;
/******/ 	}
/******/
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// object to store loaded and loading chunks
/******/ 	// undefined = chunk not loaded, null = chunk preloaded/prefetched
/******/ 	// Promise = chunk loading, 0 = chunk loaded
/******/ 	var installedChunks = {
/******/ 		0: 0
/******/ 	};
/******/
/******/ 	var deferredModules = [];
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
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
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "/TDM/k2vtdmfe/app/js/dist/";
/******/
/******/ 	var jsonpArray = window["webpackJsonp"] = window["webpackJsonp"] || [];
/******/ 	var oldJsonpFunction = jsonpArray.push.bind(jsonpArray);
/******/ 	jsonpArray.push = webpackJsonpCallback;
/******/ 	jsonpArray = jsonpArray.slice();
/******/ 	for(var i = 0; i < jsonpArray.length; i++) webpackJsonpCallback(jsonpArray[i]);
/******/ 	var parentJsonpFunction = oldJsonpFunction;
/******/
/******/
/******/ 	// add entry module to deferred list
/******/ 	deferredModules.push([232,1]);
/******/ 	// run deferred modules when ready
/******/ 	return checkDeferredModules();
/******/ })
/************************************************************************/
/******/ ({

/***/ 232:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(322);


/***/ }),

/***/ 268:
/***/ (function(module, exports, __webpack_require__) {

var map = {
	"./af": 84,
	"./af.js": 84,
	"./ar": 85,
	"./ar-dz": 86,
	"./ar-dz.js": 86,
	"./ar-kw": 87,
	"./ar-kw.js": 87,
	"./ar-ly": 88,
	"./ar-ly.js": 88,
	"./ar-ma": 89,
	"./ar-ma.js": 89,
	"./ar-sa": 90,
	"./ar-sa.js": 90,
	"./ar-tn": 91,
	"./ar-tn.js": 91,
	"./ar.js": 85,
	"./az": 92,
	"./az.js": 92,
	"./be": 93,
	"./be.js": 93,
	"./bg": 94,
	"./bg.js": 94,
	"./bm": 95,
	"./bm.js": 95,
	"./bn": 96,
	"./bn-bd": 97,
	"./bn-bd.js": 97,
	"./bn.js": 96,
	"./bo": 98,
	"./bo.js": 98,
	"./br": 99,
	"./br.js": 99,
	"./bs": 100,
	"./bs.js": 100,
	"./ca": 101,
	"./ca.js": 101,
	"./cs": 102,
	"./cs.js": 102,
	"./cv": 103,
	"./cv.js": 103,
	"./cy": 104,
	"./cy.js": 104,
	"./da": 105,
	"./da.js": 105,
	"./de": 106,
	"./de-at": 107,
	"./de-at.js": 107,
	"./de-ch": 108,
	"./de-ch.js": 108,
	"./de.js": 106,
	"./dv": 109,
	"./dv.js": 109,
	"./el": 110,
	"./el.js": 110,
	"./en-au": 111,
	"./en-au.js": 111,
	"./en-ca": 112,
	"./en-ca.js": 112,
	"./en-gb": 113,
	"./en-gb.js": 113,
	"./en-ie": 114,
	"./en-ie.js": 114,
	"./en-il": 115,
	"./en-il.js": 115,
	"./en-in": 116,
	"./en-in.js": 116,
	"./en-nz": 117,
	"./en-nz.js": 117,
	"./en-sg": 118,
	"./en-sg.js": 118,
	"./eo": 119,
	"./eo.js": 119,
	"./es": 120,
	"./es-do": 121,
	"./es-do.js": 121,
	"./es-mx": 122,
	"./es-mx.js": 122,
	"./es-us": 123,
	"./es-us.js": 123,
	"./es.js": 120,
	"./et": 124,
	"./et.js": 124,
	"./eu": 125,
	"./eu.js": 125,
	"./fa": 126,
	"./fa.js": 126,
	"./fi": 127,
	"./fi.js": 127,
	"./fil": 128,
	"./fil.js": 128,
	"./fo": 129,
	"./fo.js": 129,
	"./fr": 130,
	"./fr-ca": 131,
	"./fr-ca.js": 131,
	"./fr-ch": 132,
	"./fr-ch.js": 132,
	"./fr.js": 130,
	"./fy": 133,
	"./fy.js": 133,
	"./ga": 134,
	"./ga.js": 134,
	"./gd": 135,
	"./gd.js": 135,
	"./gl": 136,
	"./gl.js": 136,
	"./gom-deva": 137,
	"./gom-deva.js": 137,
	"./gom-latn": 138,
	"./gom-latn.js": 138,
	"./gu": 139,
	"./gu.js": 139,
	"./he": 140,
	"./he.js": 140,
	"./hi": 141,
	"./hi.js": 141,
	"./hr": 142,
	"./hr.js": 142,
	"./hu": 143,
	"./hu.js": 143,
	"./hy-am": 144,
	"./hy-am.js": 144,
	"./id": 145,
	"./id.js": 145,
	"./is": 146,
	"./is.js": 146,
	"./it": 147,
	"./it-ch": 148,
	"./it-ch.js": 148,
	"./it.js": 147,
	"./ja": 149,
	"./ja.js": 149,
	"./jv": 150,
	"./jv.js": 150,
	"./ka": 151,
	"./ka.js": 151,
	"./kk": 152,
	"./kk.js": 152,
	"./km": 153,
	"./km.js": 153,
	"./kn": 154,
	"./kn.js": 154,
	"./ko": 155,
	"./ko.js": 155,
	"./ku": 156,
	"./ku.js": 156,
	"./ky": 157,
	"./ky.js": 157,
	"./lb": 158,
	"./lb.js": 158,
	"./lo": 159,
	"./lo.js": 159,
	"./lt": 160,
	"./lt.js": 160,
	"./lv": 161,
	"./lv.js": 161,
	"./me": 162,
	"./me.js": 162,
	"./mi": 163,
	"./mi.js": 163,
	"./mk": 164,
	"./mk.js": 164,
	"./ml": 165,
	"./ml.js": 165,
	"./mn": 166,
	"./mn.js": 166,
	"./mr": 167,
	"./mr.js": 167,
	"./ms": 168,
	"./ms-my": 169,
	"./ms-my.js": 169,
	"./ms.js": 168,
	"./mt": 170,
	"./mt.js": 170,
	"./my": 171,
	"./my.js": 171,
	"./nb": 172,
	"./nb.js": 172,
	"./ne": 173,
	"./ne.js": 173,
	"./nl": 174,
	"./nl-be": 175,
	"./nl-be.js": 175,
	"./nl.js": 174,
	"./nn": 176,
	"./nn.js": 176,
	"./oc-lnc": 177,
	"./oc-lnc.js": 177,
	"./pa-in": 178,
	"./pa-in.js": 178,
	"./pl": 179,
	"./pl.js": 179,
	"./pt": 180,
	"./pt-br": 181,
	"./pt-br.js": 181,
	"./pt.js": 180,
	"./ro": 182,
	"./ro.js": 182,
	"./ru": 183,
	"./ru.js": 183,
	"./sd": 184,
	"./sd.js": 184,
	"./se": 185,
	"./se.js": 185,
	"./si": 186,
	"./si.js": 186,
	"./sk": 187,
	"./sk.js": 187,
	"./sl": 188,
	"./sl.js": 188,
	"./sq": 189,
	"./sq.js": 189,
	"./sr": 190,
	"./sr-cyrl": 191,
	"./sr-cyrl.js": 191,
	"./sr.js": 190,
	"./ss": 192,
	"./ss.js": 192,
	"./sv": 193,
	"./sv.js": 193,
	"./sw": 194,
	"./sw.js": 194,
	"./ta": 195,
	"./ta.js": 195,
	"./te": 196,
	"./te.js": 196,
	"./tet": 197,
	"./tet.js": 197,
	"./tg": 198,
	"./tg.js": 198,
	"./th": 199,
	"./th.js": 199,
	"./tk": 200,
	"./tk.js": 200,
	"./tl-ph": 201,
	"./tl-ph.js": 201,
	"./tlh": 202,
	"./tlh.js": 202,
	"./tr": 203,
	"./tr.js": 203,
	"./tzl": 204,
	"./tzl.js": 204,
	"./tzm": 205,
	"./tzm-latn": 206,
	"./tzm-latn.js": 206,
	"./tzm.js": 205,
	"./ug-cn": 207,
	"./ug-cn.js": 207,
	"./uk": 208,
	"./uk.js": 208,
	"./ur": 209,
	"./ur.js": 209,
	"./uz": 210,
	"./uz-latn": 211,
	"./uz-latn.js": 211,
	"./uz.js": 210,
	"./vi": 212,
	"./vi.js": 212,
	"./x-pseudo": 213,
	"./x-pseudo.js": 213,
	"./yo": 214,
	"./yo.js": 214,
	"./zh-cn": 215,
	"./zh-cn.js": 215,
	"./zh-hk": 216,
	"./zh-hk.js": 216,
	"./zh-mo": 217,
	"./zh-mo.js": 217,
	"./zh-tw": 218,
	"./zh-tw.js": 218
};


function webpackContext(req) {
	var id = webpackContextResolve(req);
	return __webpack_require__(id);
}
function webpackContextResolve(req) {
	if(!__webpack_require__.o(map, req)) {
		var e = new Error("Cannot find module '" + req + "'");
		e.code = 'MODULE_NOT_FOUND';
		throw e;
	}
	return map[req];
}
webpackContext.keys = function webpackContextKeys() {
	return Object.keys(map);
};
webpackContext.resolve = webpackContextResolve;
module.exports = webpackContext;
webpackContext.id = 268;

/***/ }),

/***/ 322:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(1);
var react_default = /*#__PURE__*/__webpack_require__.n(react);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/asyncToGenerator.js
var asyncToGenerator = __webpack_require__(10);
var asyncToGenerator_default = /*#__PURE__*/__webpack_require__.n(asyncToGenerator);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/toConsumableArray.js
var toConsumableArray = __webpack_require__(22);
var toConsumableArray_default = /*#__PURE__*/__webpack_require__.n(toConsumableArray);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/defineProperty.js
var defineProperty = __webpack_require__(17);
var defineProperty_default = /*#__PURE__*/__webpack_require__.n(defineProperty);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/slicedToArray.js
var slicedToArray = __webpack_require__(6);
var slicedToArray_default = /*#__PURE__*/__webpack_require__.n(slicedToArray);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/regenerator/index.js
var regenerator = __webpack_require__(4);
var regenerator_default = /*#__PURE__*/__webpack_require__.n(regenerator);

// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/taggedTemplateLiteral.js
var taggedTemplateLiteral = __webpack_require__(2);
var taggedTemplateLiteral_default = /*#__PURE__*/__webpack_require__.n(taggedTemplateLiteral);

// EXTERNAL MODULE: ./node_modules/styled-components/dist/styled-components.browser.esm.js
var styled_components_browser_esm = __webpack_require__(3);

// CONCATENATED MODULE: ./src/containers/Task/Main/styles.ts

var _templateObject, _templateObject2, _templateObject3, _templateObject4;

var Container = styled_components_browser_esm["b" /* default */].div(_templateObject || (_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    min-height: calc(100vh - 42px);\n    background-color: white;\n    position: relative;\n"])));
var WidgetWrapper = styled_components_browser_esm["b" /* default */].div(_templateObject2 || (_templateObject2 = taggedTemplateLiteral_default()(["\n    width: calc(100vw - 24px);\n    height: 280px;\n    background-color: #f2f2f2;\n"])));
var WidgetContainer = styled_components_browser_esm["b" /* default */].div(_templateObject3 || (_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    height: 277px;\n    display: flex;\n    justify-content: center;\n"])));
var FormContainer = styled_components_browser_esm["b" /* default */].div(_templateObject4 || (_templateObject4 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    width: ", "px;\n    left: calc(100% - ", "px;\n    height: auto;\n    background-color: #fff;\n"])), function (props) {
  return props.width;
}, function (props) {
  return props.width / 2;
});
// EXTERNAL MODULE: ./node_modules/react-hook-form/dist/index.esm.mjs
var index_esm = __webpack_require__(228);

// CONCATENATED MODULE: ./src/components/task/TaskForm/styles.ts

var styles_templateObject, styles_templateObject2, styles_templateObject3, styles_templateObject4, _templateObject5, _templateObject6, _templateObject7, _templateObject8, _templateObject9, _templateObject10, _templateObject11, _templateObject12, _templateObject13, _templateObject14, _templateObject15;

var Wrapper = styled_components_browser_esm["b" /* default */].div(styles_templateObject || (styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100vw;\n    height: auto;\n    display: flex;\n    justify-content: center;\n"])));

// min-width: ${(props) => props.width - 42 - 50}px;
var InnerWrapper = styled_components_browser_esm["b" /* default */].div(styles_templateObject2 || (styles_templateObject2 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    min-width: 80vw;\n    max-width: 95vw;\n    padding-bottom: 90px;\n    top: 214px;\n"])));
var styles_Container = styled_components_browser_esm["b" /* default */].div(styles_templateObject3 || (styles_templateObject3 = taggedTemplateLiteral_default()(["\n    background-color: #fff;\n    border-radius: 6px;\n    box-shadow: 0 0 21px 0 rgba(51, 51, 51, 0.2);\n"])));
var TitleContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject4 || (styles_templateObject4 = taggedTemplateLiteral_default()(["\n    display: flex;\n    justify-content: center;\n    width: auto;\n    position: relative;\n    margin: auto;\n"])));
var Title = styled_components_browser_esm["b" /* default */].div(_templateObject5 || (_templateObject5 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: flex-start;\n    font-family: Roboto;\n    font-size: 20px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: center;\n    color: var(--secondary-color);\n    margin-bottom: 12px;\n"])));
var Body = styled_components_browser_esm["b" /* default */].div(_templateObject6 || (_templateObject6 = taggedTemplateLiteral_default()(["\n    margin-top: 15px;\n    position: relative;\n    padding: 0px 44px 44px 50px;\n"])));
var ResetButton = styled_components_browser_esm["b" /* default */].div(_templateObject7 || (_templateObject7 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    right: 0px;\n    top: 0px;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1683f2;\n    display: flex;\n    gap: 6px;\n    align-items: center;\n    cursor: pointer;\n"])));
var CloseButton = styled_components_browser_esm["b" /* default */].div(_templateObject8 || (_templateObject8 = taggedTemplateLiteral_default()(["\n    bottom: -107px;\n    left: ", "px;\n    position: absolute;\n    width: 54px;\n    height: 54px;\n    border-radius: 50%;\n    background-color: #1483f3;\n    display: flex;\n    justify-content: center;\n    align-items: center;\n    cursor: pointer;\n"])), function (props) {
  return props.width / 2 - 27;
});
var Icon = styled_components_browser_esm["b" /* default */].img(_templateObject9 || (_templateObject9 = taggedTemplateLiteral_default()(["\n"])));
var Form = styled_components_browser_esm["b" /* default */].form(_templateObject10 || (_templateObject10 = taggedTemplateLiteral_default()(["\n"])));
var DashedContainer = styled_components_browser_esm["b" /* default */].div(_templateObject11 || (_templateObject11 = taggedTemplateLiteral_default()(["\n    height: 5px;\n    width: 100%;\n\n"])));
var StickyHeader = styled_components_browser_esm["b" /* default */].div(_templateObject12 || (_templateObject12 = taggedTemplateLiteral_default()(["\n    position: sticky;\n    top: 0;\n    padding: 20px 44px 0px 50px;\n    z-index: 100;\n    background-color: white;\n"])));
var RegularContainer = styled_components_browser_esm["b" /* default */].div(_templateObject13 || (_templateObject13 = taggedTemplateLiteral_default()(["\n    height: 3px;\n    width: 100%;\n    background-image: ", ";\n    background-size: ", ";\n    position: absolute;\n    bottom: 0px;\n\n"])), function (props) {
  if (props.dashed) {
    return "linear-gradient(90deg, ".concat(props.color1, " 50%, transparent 50%)");
  }
  return "linear-gradient(to right, ".concat(props.color1, ",  ").concat(props.color2, ")");
}, function (props) {
  return props.dashed ? '30px 10px,40px 10px,40px 10px,40px 10px' : '';
});
var SaveFormButton = styled_components_browser_esm["b" /* default */].button(_templateObject14 || (_templateObject14 = taggedTemplateLiteral_default()(["\n    display: none;\n"])));
var MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span(_templateObject15 || (_templateObject15 = taggedTemplateLiteral_default()(["\n    color: red;\n"])));
// CONCATENATED MODULE: ./src/images/revert-icon.svg
/* harmony default export */ var revert_icon = ("js/dist/7ce9dc66c632361100775a2c723eb394.svg");
// EXTERNAL MODULE: ./node_modules/react/jsx-runtime.js
var jsx_runtime = __webpack_require__(0);

// CONCATENATED MODULE: ./src/components/task/TaskForm/HeaderTitleBorder.tsx


function HeaderTitleBorder(props) {
  var dashed = props.dashed,
    color1 = props.color1,
    color2 = props.color2;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(RegularContainer, {
    dashed: dashed,
    color1: color1,
    color2: color2
  });
}
/* harmony default export */ var TaskForm_HeaderTitleBorder = (HeaderTitleBorder);
// CONCATENATED MODULE: ./src/components/task/TaskForm/index.tsx






function TaskForm(props) {
  var title = props.title,
    width = props.width,
    hideReset = props.hideReset,
    children = props.children,
    mandatory = props.mandatory,
    onReset = props.onReset,
    dashed = props.dashed,
    title_border_color = props.title_border_color,
    title_border_color2 = props.title_border_color2;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(InnerWrapper, {
      width: width,
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Container, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(StickyHeader, {
          children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TitleContainer, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Title, {
              children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(MadatoryAsterisk, {
                children: mandatory && false ? '*' : ''
              })]
            }), hideReset ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(ResetButton, {
              onClick: onReset,
              children: ["Clear form", /*#__PURE__*/Object(jsx_runtime["jsx"])(Icon, {
                src: revert_icon
              })]
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskForm_HeaderTitleBorder, {
              dashed: dashed,
              color1: title_border_color || '#cccccc',
              color2: title_border_color2 || title_border_color || '#cccccc'
            })]
          })
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Body, {
          children: children
        })]
      })
    })
  });
}
/* harmony default export */ var task_TaskForm = (TaskForm);
// CONCATENATED MODULE: ./src/containers/Task/Main/config.ts
var stepsConfig = {
  be: {
    width: 968,
    title: 'Data movement settings',
    title_border_color: '#1483f3',
    mandatory: true,
    mandatoryFields: ['be_name', 'selected_logical_units', 'tableList']
  },
  source: {
    width: 968,
    title: 'Source environment settings',
    title_border_color: '#8444f0',
    mandatory: true
  },
  source_data_subset: {
    width: 968,
    title: 'Data subset settings',
    title_border_color: '#8444f0',
    dashed: true,
    mandatory: true
  },
  target_data_subset: {
    width: 968,
    title: 'Data subset settings',
    title_border_color: '#1483f3',
    dashed: false,
    mandatory: true
  },
  test_data_store: {
    width: 968,
    title: 'Test data store',
    title_border_color: '#8444f0',
    title_border_color2: '#1483f3',
    mandatory: true
  },
  target: {
    width: 968,
    title: 'Target environment settings',
    title_border_color: '#1483f3',
    mandatory: true
  },
  scheduler: {
    width: 968,
    title: 'Task schedule settings'
  },
  be_advanced: {
    width: 968,
    title: 'Advanced settings',
    hideReset: true
  },
  post_execution_process: {
    width: 968,
    title: 'POST EXECUTION PROCESS'
  },
  pre_execution_process: {
    width: 968,
    title: 'PRE EXECUTION PROCESS'
  },
  task_variables: {
    width: 968,
    title: 'TASK VARIABLES'
  },
  task_title: {
    width: 968,
    title: 'Task name'
  }
};
var taskTypeHints = {
  '10000': ['Extract the data from source environment into the TDM warehouse'],
  '10100': ['Refresh data from source and load (provision) it to target environment'],
  '10101': ['Extract the data from source environment', 'Provision (load) the data to the target environment and mark the entities as reserved'],
  '10110': ['Extract the data from source environment', 'Delete and reprovision (reload) the data to the target environment'],
  '10111': ['Extract data from source environment', 'Delete and reload (reprovision) data to target environment', 'Mark entities as reserved'],
  '00100': ['Provision data to the target environment'],
  '00101': ['Get data from TDM warehouse and load (provision) it to target environment', 'Mark entities as reserved'],
  '00110': ['Get the data from the TDM warehouse', 'Delete and reprovision (reload) it to the target environment'],
  '00111': ['Get data from TDM warehouse', 'Delete and reload (reprovision) data to target environment', 'Mark entities as reserved'],
  '00010': ['Delete (clean) entities from target environment'],
  '00001': ['Reserve entities in the target environment.'],
  '01000': ['Generate synthetic entities and save them into the TDM warehouse.'],
  '01100': ['Generate synthetic entities and save them into the TDM warehouse.', 'Load the synthetic entities to the target environment'],
  '11100': ['Generate synthetic entities and save them into the TDM warehouse.', 'Load the synthetic entities to the target environment.'],
  '01101': ['Generate synthetic entities and save them into the TDM warehouse.', 'Load the synthetic entities to the target environment and mark them as reserved.'],
  '11101': ['Generate synthetic entities and save them into the TDM warehouse.', 'Load the synthetic entities to the target environment and mark them as reserved.']
};
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSourceSettings/styles.ts

var DataSourceSettings_styles_templateObject, DataSourceSettings_styles_templateObject2, DataSourceSettings_styles_templateObject3, DataSourceSettings_styles_templateObject4, styles_templateObject5, styles_templateObject6, styles_templateObject7, styles_templateObject8, styles_templateObject9, styles_templateObject10, styles_templateObject11, styles_templateObject12, styles_templateObject13, styles_templateObject14;

var styles_Wrapper = styled_components_browser_esm["b" /* default */].div(DataSourceSettings_styles_templateObject || (DataSourceSettings_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    gap: 20px;\n    align-items: flex-start;\n"])));
var DataSourceTypes = styled_components_browser_esm["b" /* default */].div(DataSourceSettings_styles_templateObject2 || (DataSourceSettings_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    align-items: flex-start;\n    gap: 15px;\n    padding-bottom: 30px;\n    margin-top: 10px;\n    border-bottom: solid 1px #ccc;\n    width: 100%;\n"])));
var MaskDataContainer = styled_components_browser_esm["b" /* default */].div(DataSourceSettings_styles_templateObject3 || (DataSourceSettings_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 13px;\n    align-items: center;\n    margin-top: 27px;\n    align-self: center;\n    min-width: 203px;\n"])));
var DataSourceTitle = styled_components_browser_esm["b" /* default */].div(DataSourceSettings_styles_templateObject4 || (DataSourceSettings_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n"])));
var styles_Icon = styled_components_browser_esm["b" /* default */].img(styles_templateObject5 || (styles_templateObject5 = taggedTemplateLiteral_default()(["\n\n"])));
var EnvironmentsContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject6 || (styles_templateObject6 = taggedTemplateLiteral_default()(["\n    display: ", ";\n    align-items: flex-start;\n    flex-direction: column;\n    width: 560px;\n    gap: 10px;\n\n"])), function (props) {
  return props.data_source ? 'flex' : 'none';
});
var FetchDataPolicyContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject7 || (styles_templateObject7 = taggedTemplateLiteral_default()(["\n    margin-top: 10px;\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    gap: 20px;\n"])));
var styles_Title = styled_components_browser_esm["b" /* default */].div(styles_templateObject8 || (styles_templateObject8 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    color: #2e2e2e;\n"])));
var TitleBold = styled_components_browser_esm["b" /* default */].div(styles_templateObject9 || (styles_templateObject9 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    color: #2e2e2e;\n"])));
var SyntheticContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject10 || (styles_templateObject10 = taggedTemplateLiteral_default()(["\n    border-left: ", ";\n    display: flex;\n    gap: 30px;\n    align-items: center;\n    padding: ", " ;\n"])), function (props) {
  return props.widthBorder ? ' #ccc solid 1px' : '';
}, function (props) {
  return props.widthBorder ? ' 0px 0px 0px 20px' : '';
});
var SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div(styles_templateObject11 || (styles_templateObject11 = taggedTemplateLiteral_default()(["\n    align-self: flex-start;\n    display: flex;\n    flex-direction: column;\n    gap: 10px;\n"])));
var EnvironmentAndMaskData = styled_components_browser_esm["b" /* default */].div(styles_templateObject12 || (styles_templateObject12 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 40px;\n"])));
var DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject13 || (styles_templateObject13 = taggedTemplateLiteral_default()(["\n    margin-bottom: 10px;\n"])));
var DataSourceContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject14 || (styles_templateObject14 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 25px;\n    width: 100%;\n"])));
// CONCATENATED MODULE: ./src/components/radio/styles.ts

var radio_styles_templateObject, radio_styles_templateObject2, radio_styles_templateObject3, radio_styles_templateObject4;

var radio_styles_Container = styled_components_browser_esm["b" /* default */].label(radio_styles_templateObject || (radio_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    cursor: pointer;\n"])));
var radio_styles_Title = styled_components_browser_esm["b" /* default */].span(radio_styles_templateObject2 || (radio_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    text-align: center;\n    color: #2e2e2e;\n"])));
var RadioInput = styled_components_browser_esm["b" /* default */].input(radio_styles_templateObject3 || (radio_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    margin: 0px 5px 0px 0px !important;\n    width: 20px;\n    height: 20px;\n"])));
var radio_styles_Icon = styled_components_browser_esm["b" /* default */].img(radio_styles_templateObject4 || (radio_styles_templateObject4 = taggedTemplateLiteral_default()(["\n\n"])));
// CONCATENATED MODULE: ./src/components/radio/index.tsx




function Radio(props) {
  var title = props.title,
    name = props.name,
    value = props.value,
    onChange = props.onChange,
    selectedValue = props.selectedValue,
    disabled = props.disabled,
    tooltip = props.tooltip;
  var onChangeLocal = Object(react["useCallback"])(function (event) {
    onChange(event.target.value || null);
  }, [onChange]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(radio_styles_Container, {
    title: tooltip,
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RadioInput, {
      type: "radio",
      name: name,
      value: value,
      checked: value === selectedValue,
      onChange: onChangeLocal,
      disabled: disabled
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(radio_styles_Title, {
      children: title
    })]
  });
}
/* harmony default export */ var components_radio = (Radio);
// CONCATENATED MODULE: ./src/components/DataGenerationParameters/styles.ts

var DataGenerationParameters_styles_templateObject, DataGenerationParameters_styles_templateObject2, DataGenerationParameters_styles_templateObject3, DataGenerationParameters_styles_templateObject4, DataGenerationParameters_styles_templateObject5, DataGenerationParameters_styles_templateObject6, DataGenerationParameters_styles_templateObject7, DataGenerationParameters_styles_templateObject8, DataGenerationParameters_styles_templateObject9, DataGenerationParameters_styles_templateObject10, DataGenerationParameters_styles_templateObject11, DataGenerationParameters_styles_templateObject12, DataGenerationParameters_styles_templateObject13, DataGenerationParameters_styles_templateObject14, styles_templateObject15, _templateObject16;

var DataGenerationParameters_styles_Container = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject || (DataGenerationParameters_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 30px;\n    position: relative;\n    width: 100%;\n"])));
var ParamsContainer = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject2 || (DataGenerationParameters_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 350px;\n"])));
var ParamsList = styled_components_browser_esm["b" /* default */].ul(DataGenerationParameters_styles_templateObject3 || (DataGenerationParameters_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    padding: 0;\n    width: 100%;\n    margin: 0;\n    max-height: 235px;\n    overflow: auto;\n    max-width: 100%;\n    border-radius: 3px;\n    box-shadow: 0 0 9px 1px rgba(51, 51, 51, 0.2);\n    border: solid 1px #ccc;\n    background-color: #fff;\n\n"])));
var ParamsItem = styled_components_browser_esm["b" /* default */].li(DataGenerationParameters_styles_templateObject4 || (DataGenerationParameters_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    max-height: 250px;\n    overflow: auto;\n    overflow-x: hidden;\n    padding: 13px;\n    display: flex;\n    align-items: center;\n    justify-content: space-between;\n    background-color: ", ";\n"])), function (props) {
  return props.chosen ? '#f2f2f2' : 'transparent';
});
var ParamsItemText = styled_components_browser_esm["b" /* default */].span(DataGenerationParameters_styles_templateObject5 || (DataGenerationParameters_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    width: calc(100% - 16px);\n"])));
var DataGenerationParameters_styles_Icon = styled_components_browser_esm["b" /* default */].img(DataGenerationParameters_styles_templateObject6 || (DataGenerationParameters_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    width: 16px;\n"])));
var DummyIcon = styled_components_browser_esm["b" /* default */].img(DataGenerationParameters_styles_templateObject7 || (DataGenerationParameters_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    padding-right: 16px;\n"])));
var Leftside = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject8 || (DataGenerationParameters_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    border-right: ", ";\n    display: flex;\n    flex-direction: column;\n    gap: 25px;\n    padding-right: 30px;\n"])), function (props) {
  return props.hideBorders ? '' : '1px solid #ccc';
});
var Middle = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject9 || (DataGenerationParameters_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    border-right: ", ";\n    padding-right: 30px;\n"])), function (props) {
  return props.hideBorders ? '' : '1px solid #ccc';
});
var RightSide = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject10 || (DataGenerationParameters_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    flex: 1;\n"])));
var DummyImg = styled_components_browser_esm["b" /* default */].img(DataGenerationParameters_styles_templateObject11 || (DataGenerationParameters_styles_templateObject11 = taggedTemplateLiteral_default()(["\n"])));
var styles_SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject12 || (DataGenerationParameters_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    align-self: flex-start;\n    display: flex;\n    flex-direction: column;\n    gap: 10px;\n"])));
var Seprator = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject13 || (DataGenerationParameters_styles_templateObject13 = taggedTemplateLiteral_default()(["\n    border-right: 1px solid #ccc;\n    width: 1px;\n    position: absolute;\n    height: calc(100% + 110px);\n    top: -30px;\n    left: 400px;\n"])));
var PopoverTemplate = styled_components_browser_esm["b" /* default */].div(DataGenerationParameters_styles_templateObject14 || (DataGenerationParameters_styles_templateObject14 = taggedTemplateLiteral_default()(["\n    padding: 10px;\n    font-family: Roboto;\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    width: max-content;\n    max-width: 250px;\n    max-height: 400px;\n    border-radius:3px;\n      border: solid 1px #ccc;\n      background-color: #fff;\n"])));
var FieldDescription = styled_components_browser_esm["b" /* default */].div(styles_templateObject15 || (styles_templateObject15 = taggedTemplateLiteral_default()(["\n"])));
var styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div(_templateObject16 || (_templateObject16 = taggedTemplateLiteral_default()(["\n    border-bottom:  ", ";\n    padding-bottom: 10px;\n"])), function (props) {
  return props.hideBorders ? '' : '1px solid #ccc';
});
// CONCATENATED MODULE: ./src/components/Input/styles.ts

var Input_styles_templateObject, Input_styles_templateObject2, Input_styles_templateObject3, Input_styles_templateObject4, Input_styles_templateObject5;

var Input_styles_Container = styled_components_browser_esm["b" /* default */].div(Input_styles_templateObject || (Input_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: ", ";\n    position: relative;\n"])), function (props) {
  return props.width || '100%';
});
var Input_styles_Title = styled_components_browser_esm["b" /* default */].div(Input_styles_templateObject2 || (Input_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    margin-bottom: 7px;\n"])));
var styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span(Input_styles_templateObject3 || (Input_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    color: red;\n"])));
var ErrorContainer = styled_components_browser_esm["b" /* default */].small(Input_styles_templateObject4 || (Input_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    color: #ed5565;\n"])));
var Input = styled_components_browser_esm["b" /* default */].input(Input_styles_templateObject5 || (Input_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    height: unset;\n    font-family: Roboto;\n    font-size: 15px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n    padding: 9px 10px;\n    border-radius: 3px;\n    border: solid 1px #ccc;\n    width: -webkit-fill-available;\n    width: -moz-available;\n    &:placeholder{\n        font-size: 15px;\n        font-weight: normal;\n        font-stretch: normal;\n        font-style: normal;\n        line-height: 1.33;\n        letter-spacing: normal;\n        text-align: left;\n        color: #999;\n    }\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Main/TaskContext.ts

var TaskContext = /*#__PURE__*/Object(react["createContext"])({
  resetField: null,
  unregister: null,
  register: null,
  clearErrors: null,
  errors: null,
  submittedForm: null,
  saveForm: function saveForm() {},
  taskData: {
    globals: []
  },
  allLogicalUnits: [],
  copy: false,
  statusesFuncMap: null,
  scope: {},
  config_params: {}
});
// CONCATENATED MODULE: ./src/components/FieldError/styles.ts

var FieldError_styles_templateObject;

var FieldError_styles_Container = styled_components_browser_esm["b" /* default */].small(FieldError_styles_templateObject || (FieldError_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: ", ";\n    color: ", ";\n    position: ", ";\n    bottom: ", ";\n    left: ", ";\n    font-size: ", "; \n    font-weight: ", ";\n    white-space: ", ";\n"])), function (props) {
  return props.visible ? 'block' : 'none';
}, function (props) {
  return props.isInfo ? '#2e2e2e' : '#ed5565';
}, function (props) {
  return props.position ? 'relative' : 'absolute';
}, function (props) {
  return props.position ? '' : '-18px';
}, function (props) {
  return props.position ? '' : '2px';
}, function (props) {
  return props.isInfo ? '15px' : '';
}, function (props) {
  return props.isInfo ? '500' : '';
}, function (props) {
  return props.width ? 'pre-line' : 'nowrap';
});
// CONCATENATED MODULE: ./src/components/FieldError/index.tsx




function FieldError(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    submittedForm = _useContext.submittedForm;
  var error = props.error,
    submit = props.submit,
    relativePosition = props.relativePosition,
    info = props.info,
    width = props.width;
  var visible = false;
  if (submit) {
    visible = true;
  }
  if (submit === undefined && submittedForm) {
    visible = true;
  }
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(FieldError_styles_Container, {
    width: width,
    visible: visible,
    position: relativePosition,
    isInfo: info,
    children: error
  });
}
/* harmony default export */ var components_FieldError = (FieldError);
// CONCATENATED MODULE: ./src/components/Input/index.tsx





var InputTypes = /*#__PURE__*/function (InputTypes) {
  InputTypes["number"] = "number";
  InputTypes["text"] = "text";
  return InputTypes;
}({});
function TDMInput(props) {
  var title = props.title,
    value = props.value,
    onChange = props.onChange,
    name = props.name,
    type = props.type,
    mandatory = props.mandatory,
    placeholder = props.placeholder,
    width = props.width,
    min = props.min,
    max = props.max,
    error = props.error,
    disabled = props.disabled;
  var onChangeLocal = Object(react["useCallback"])(function (event) {
    if (type === 'number') {
      onChange(isNaN(event.target.valueAsNumber) ? undefined : event.target.valueAsNumber);
    } else {
      onChange(event.target.value);
    }
  }, [onChange, type]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Input_styles_Container, {
    width: width,
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Input_styles_Title, {
      children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_MadatoryAsterisk, {
        children: mandatory && title ? '*' : ''
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Input
    // required={mandatory}
    , {
      min: min,
      max: max,
      placeholder: placeholder,
      type: type,
      name: name,
      value: value || '',
      onChange: onChangeLocal,
      disabled: disabled
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
      error: error
    })]
  });
}
/* harmony default export */ var components_Input = (TDMInput);
// EXTERNAL MODULE: ./node_modules/@uidotdev/usehooks/index.js
var usehooks = __webpack_require__(35);

// CONCATENATED MODULE: ./src/components/fabricWidget/index.tsx






function FabricWidget(props) {
  var luName = props.luName,
    flowName = props.flowName,
    editor = props.editor,
    error = props.error,
    updateValues = props.updateValues,
    saveRef = props.saveRef;
  var ref = Object(react["useRef"])(null);
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    widgetRefData = _useState2[0],
    setWidgetRefData = _useState2[1];
  var refclickAway = Object(usehooks["a" /* useClickAway */])(function () {
    var editors = widgetRefData === null || widgetRefData === void 0 ? void 0 : widgetRefData.getValues();
    if (editors.length >= 0) {
      updateValues(editors.map(function (it) {
        return {
          value: it.value,
          name: it.name,
          schema: it.schema
        };
      }));
    }
  });
  Object(react["useEffect"])(function () {
    var _window, _window$k2widgets;
    var onWidgetLoad = function onWidgetLoad(data) {
      var editors = !Array.isArray(editor) ? [editor] : editor;
      editors.forEach(function (editor) {
        if (editor && editor.schema2) {
          data.updateValue(editor.name, editor.value, editor.schema2);
        }
      });
      setWidgetRefData(data);
      saveRef(data);
      // save it in task Data
    };
    var disposeWidget = function disposeWidget(ref) {
      if (!ref) {
        return;
      }
      console.log('============');
      console.log(ref.getValues());
      console.log('============');
      // const editors: any = ref.getValues();
      // // if (editors.length >= 0) {
      // //     updateValues(
      // //         editors.map((it: any) => ({
      // //             value: it.value,
      // //             name: it.name,
      // //         }))
      // //     );
      // // }
    };
    (_window = window) === null || _window === void 0 ? void 0 : (_window$k2widgets = _window.k2widgets) === null || _window$k2widgets === void 0 ? void 0 : _window$k2widgets.createWidget(ref.current, 'plugins', onWidgetLoad, disposeWidget, {
      plugins: !Array.isArray(editor) ? [editor] : editor,
      theme: 'light',
      luName: luName,
      flowName: flowName
    });
  }, [ref, setWidgetRefData, luName, flowName]);
  Object(react["useEffect"])(function () {
    if (!widgetRefData) {
      return;
    }
    var values = widgetRefData.getValues();
    var keys = values.map(function (it) {
      return it.name;
    }).filter(function (it) {
      return it;
    });
    var editor_names = editor.map(function (it) {
      return it.name;
    });
    var plugins_to_add = [];
    for (var i = 0; i < editor_names.length; i++) {
      var name = editor_names[i];
      if (keys.indexOf(name) < 0) {
        plugins_to_add.push(editor[i]);
      }
    }
    for (var _i = 0; _i < keys.length; _i++) {
      var key = keys[_i];
      if (editor_names.indexOf(key) < 0) {
        widgetRefData.removePluginByName(keys[_i]);
      }
    }
    if (plugins_to_add.length > 0) {
      widgetRefData.addPlugins(plugins_to_add);
    }
    // if (editor.length > keys.length) {
    //     for (let i = 0; i < editor.length; i++) {
    //         if (keys.indexOf(editor[i].name) < 0) {
    //             widgetRefData.addPlugins([editor[i]]);
    //             break;
    //         }
    //     }
    // } else if (editor.length < keys.length) {
    //     const editorKeys = editor.map((it: any) => it.name);
    //     for (let i = 0; i < keys.length; i++) {
    //         if (editorKeys.indexOf(keys[i]) < 0) {
    //             widgetRefData.removePluginByName(keys[i]);
    //         }
    //     }
    // }
  }, [editor, widgetRefData]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])("div", {
    ref: refclickAway,
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
      ref: ref
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
      error: error
    })]
  });
}
/* harmony default export */ var fabricWidget = (FabricWidget);
// CONCATENATED MODULE: ./src/images/info-icon.svg
/* harmony default export */ var info_icon = ("js/dist/92c8be0f1e038ef4f413ddf2465983fb.svg");
// CONCATENATED MODULE: ./src/images/widgetdemo.png
/* harmony default export */ var widgetdemo = ("js/dist/e936100e064c6659e67aed82dd3610e1.png");
// CONCATENATED MODULE: ./src/components/NumberOfEntities/styles.ts

var NumberOfEntities_styles_templateObject;

var NumberOfEntities_styles_Container = styled_components_browser_esm["b" /* default */].div(NumberOfEntities_styles_templateObject || (NumberOfEntities_styles_templateObject = taggedTemplateLiteral_default()(["\n"])));
// CONCATENATED MODULE: ./src/components/NumberOfEntities/index.tsx

function ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }





function NumberOfEntities(props) {
  var _errors$num_of_entiti;
  var width = props.width,
    title = props.title,
    placeholder = props.placeholder;
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var num_of_entities = taskData.num_of_entities,
    maxToCopy = taskData.maxToCopy,
    clone_ind = taskData.clone_ind;
  var onChange = Object(react["useCallback"])(function (value) {
    saveForm({
      num_of_entities: value
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(NumberOfEntities_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, _objectSpread(_objectSpread({}, register('num_of_entities', {
      required: 'Populate Number of Entities',
      min: {
        value: 1,
        message: 'Minimum Entites to Copy is 1'
      },
      max: {
        value: maxToCopy,
        message: "Maximum Entites to Copy is ".concat(maxToCopy)
      }
    })), {}, {
      disabled: clone_ind,
      width: width || '100%',
      name: "num_of_entities",
      mandatory: true,
      min: 1,
      placeholder: placeholder,
      type: InputTypes.number,
      value: clone_ind ? 1 : num_of_entities,
      onChange: onChange,
      title: title,
      max: maxToCopy,
      error: (_errors$num_of_entiti = errors.num_of_entities) === null || _errors$num_of_entiti === void 0 ? void 0 : _errors$num_of_entiti.message
    }))
  });
}
/* harmony default export */ var components_NumberOfEntities = (NumberOfEntities);
// CONCATENATED MODULE: ./src/components/checkbox/styles.ts

var checkbox_styles_templateObject, checkbox_styles_templateObject2, checkbox_styles_templateObject3, checkbox_styles_templateObject4;

var checkbox_styles_Container = styled_components_browser_esm["b" /* default */].label(checkbox_styles_templateObject || (checkbox_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    cursor: pointer;\n"])));
var checkbox_styles_Title = styled_components_browser_esm["b" /* default */].span(checkbox_styles_templateObject2 || (checkbox_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    text-align: center;\n    color: #2e2e2e;\n    white-space: nowrap;\n    overflow: hidden;\n    text-overflow: ellipsis;\n"])));
var CheckboxInput = styled_components_browser_esm["b" /* default */].input(checkbox_styles_templateObject3 || (checkbox_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    margin: 0px 10px 0px 0px !important;\n    width: 20px;\n    height: 20px;\n"])));
var checkbox_styles_Icon = styled_components_browser_esm["b" /* default */].img(checkbox_styles_templateObject4 || (checkbox_styles_templateObject4 = taggedTemplateLiteral_default()(["\n\n"])));
// CONCATENATED MODULE: ./src/components/checkbox/index.tsx




function Checkbox(props) {
  var title = props.title,
    name = props.name,
    value = props.value,
    onChange = props.onChange,
    disabled = props.disabled;
  var onChangeLocal = Object(react["useCallback"])(function (event) {
    onChange(event.target.checked);
  }, [onChange]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(checkbox_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CheckboxInput, {
      type: "checkbox",
      disabled: disabled,
      name: name,
      checked: value,
      onChange: onChangeLocal
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(checkbox_styles_Title, {
      children: title
    })]
  });
}
/* harmony default export */ var components_checkbox = (Checkbox);
// CONCATENATED MODULE: ./src/components/TooltipPopover/styles.ts

var TooltipPopover_styles_templateObject;

var TooltipContainer = styled_components_browser_esm["b" /* default */].div(TooltipPopover_styles_templateObject || (TooltipPopover_styles_templateObject = taggedTemplateLiteral_default()(["\n  position: relative;\n"])));
// EXTERNAL MODULE: ./node_modules/react-tiny-popover/dist/Popover.js
var Popover = __webpack_require__(33);

// CONCATENATED MODULE: ./src/components/TooltipPopover/index.tsx

// Tooltip.tsx





var TooltipPopover_TooltipPopover = function TooltipPopover(_ref) {
  var children = _ref.children,
    position = _ref.position,
    body = _ref.body,
    align = _ref.align;
  var _useHover = Object(usehooks["b" /* useHover */])(),
    _useHover2 = slicedToArray_default()(_useHover, 2),
    ref = _useHover2[0],
    hovering = _useHover2[1];
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TooltipContainer, {
    ref: ref,
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
      reposition: false,
      isOpen: hovering,
      align: align,
      positions: [position],
      content: body,
      children: children
    })
  });
};
/* harmony default export */ var components_TooltipPopover = (TooltipPopover_TooltipPopover);
// CONCATENATED MODULE: ./src/components/task/DataMovmentSettings/styles.ts

var DataMovmentSettings_styles_templateObject, DataMovmentSettings_styles_templateObject2, DataMovmentSettings_styles_templateObject3, DataMovmentSettings_styles_templateObject4, DataMovmentSettings_styles_templateObject5, DataMovmentSettings_styles_templateObject6, DataMovmentSettings_styles_templateObject7, DataMovmentSettings_styles_templateObject8;

var DataMovmentSettings_styles_Container = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject || (DataMovmentSettings_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
var TabsContainer = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject2 || (DataMovmentSettings_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    gap: 48px;\n    padding-bottom: 28px;\n"])));
var TabItem = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject3 || (DataMovmentSettings_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    display: flex;\n    align-items: center;\n    gap: 13px;\n    cursor: pointer;\n"])));
var DataMovmentSettings_styles_Icon = styled_components_browser_esm["b" /* default */].img(DataMovmentSettings_styles_templateObject4 || (DataMovmentSettings_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var DataMovmentSettings_styles_Title = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject5 || (DataMovmentSettings_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    padding: 22px 0px;\n"])));
var TabTitle = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject6 || (DataMovmentSettings_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    position: relative;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n"])));
var SelectedTab = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject7 || (DataMovmentSettings_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    height: 3px;\n    width: 100%;\n    border: solid 1px #f4f3ef;\n    background-color: #1483f3;\n"])));
var styles_Body = styled_components_browser_esm["b" /* default */].div(DataMovmentSettings_styles_templateObject8 || (DataMovmentSettings_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
// CONCATENATED MODULE: ./src/images/entity-icon.svg
/* harmony default export */ var entity_icon = ("js/dist/4aa35f4bd77ca777c9c98d64b8169384.svg");
// CONCATENATED MODULE: ./src/images/table-icon.svg
/* harmony default export */ var table_icon = ("js/dist/f46cfe60fb2ec3de6aea40a9b9ea328a.svg");
// CONCATENATED MODULE: ./src/components/Select/styles.ts

var Select_styles_templateObject, Select_styles_templateObject2, Select_styles_templateObject3, Select_styles_templateObject4, Select_styles_templateObject5, Select_styles_templateObject6, Select_styles_templateObject7;

var Select_styles_Container = styled_components_browser_esm["b" /* default */].div(Select_styles_templateObject || (Select_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: ", ";\n    min-width: ", ";\n    max-width: ", ";\n    max-width: 100%;\n    position: relative;\n    font-size: 16px;\n"])), function (props) {
  return props.width || '100%';
}, function (props) {
  return props.minWidth || '';
}, function (props) {
  return props.maxWidth || '';
});
var Select_styles_Title = styled_components_browser_esm["b" /* default */].div(Select_styles_templateObject2 || (Select_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    margin-bottom: 7px;\n    display: flex;\n    align-items:center;\n    gap: 13px;\n"])));
var OptionContainer = styled_components_browser_esm["b" /* default */].div(Select_styles_templateObject3 || (Select_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    justify-content: space-between;\n    min-height: 25px;\n"])));
var Select_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span(Select_styles_templateObject4 || (Select_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    color: red;\n"])));
var Select_styles_Icon = styled_components_browser_esm["b" /* default */].img(Select_styles_templateObject5 || (Select_styles_templateObject5 = taggedTemplateLiteral_default()(["\n"])));
var DescriptionContainer = styled_components_browser_esm["b" /* default */].div(Select_styles_templateObject6 || (Select_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    right: 42px;\n    bottom: 7px;\n"])));
var styles_PopoverTemplate = styled_components_browser_esm["b" /* default */].div(Select_styles_templateObject7 || (Select_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    padding: 10px;\n    font-family: Roboto;\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    width: max-content;\n    max-width: 250px;\n    max-height: 400px;\n    border-radius:3px;\n      border: solid 1px #ccc;\n      background-color: #fff;\n"])));
// EXTERNAL MODULE: ./node_modules/react-select/dist/index-baa8dc4f.esm.js + 1 modules
var index_baa8dc4f_esm = __webpack_require__(21);

// EXTERNAL MODULE: ./node_modules/react-select/dist/react-select.esm.js + 7 modules
var react_select_esm = __webpack_require__(229);

// CONCATENATED MODULE: ./src/components/Select/index.tsx


function Select_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Select_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Select_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Select_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }










function TDMSelect(props) {
  var title = props.title,
    options = props.options,
    value = props.value,
    mandatory = props.mandatory,
    loading = props.loading,
    onChange = props.onChange,
    placeholder = props.placeholder,
    width = props.width,
    minWidth = props.minWidth,
    maxWidth = props.maxWidth,
    error = props.error,
    isMulti = props.isMulti,
    isClearable = props.isClearable,
    disabled = props.disabled,
    enableSelectAll = props.enableSelectAll,
    titleIcon = props.titleIcon,
    maxMenuHeight = props.maxMenuHeight;
  var Option = index_baa8dc4f_esm["o" /* c */].Option;
  var _useHover = Object(usehooks["b" /* useHover */])(),
    _useHover2 = slicedToArray_default()(_useHover, 2),
    ref = _useHover2[0],
    hovering = _useHover2[1];
  var getValues = function getValues(item) {
    var temp = [];
    if (Array.isArray(item)) {
      item.forEach(function (it) {
        temp = temp.concat(getValues(it));
      });
    } else if (item.options) {
      temp = temp.concat(getValues(item.options));
    } else {
      temp = [item];
    }
    return temp;
  };
  var localOptions = Object(react["useMemo"])(function () {
    if (isMulti && enableSelectAll) {
      var allValues = getValues(options || []);
      if (!value || value.length === 0 || allValues.length !== value.length) {
        return [{
          label: 'All',
          value: 'All'
        }].concat(options);
      }
    }
    return options;
  }, [options, enableSelectAll, isMulti, value]);
  var onLocalChange = Object(react["useCallback"])(function (item) {
    if (isMulti) {
      if (item.findIndex(function (it) {
        return it.label === 'All';
      }) >= 0) {
        var temp = getValues(options);
        onChange(temp);
        return;
      }
    }
    onChange(item);
  }, [onChange, isMulti, options]);
  var ValueOption = function ValueOption(props) {
    var _props$data;
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Option, Select_objectSpread(Select_objectSpread({}, props), {}, {
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(OptionContainer, {
        children: [props.data.label, props !== null && props !== void 0 && (_props$data = props.data) !== null && _props$data !== void 0 && _props$data.description ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_Icon, {
          src: info_icon
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })
    }));
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Select_styles_Container, {
    width: width,
    minWidth: minWidth,
    maxWidth: maxWidth,
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Select_styles_Title, {
      children: [titleIcon ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_Icon, {
        src: titleIcon
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])("span", {
        children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_MadatoryAsterisk, {
          children: mandatory && title ? '*' : ''
        })]
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(react_select_esm["a" /* default */], {
      isDisabled: disabled,
      placeholder: placeholder,
      className: "basic-single",
      value: value || null,
      defaultValue: isMulti ? [] : null,
      classNamePrefix: "select",
      isLoading: loading,
      isClearable: isClearable,
      isSearchable: true,
      options: localOptions,
      onChange: onLocalChange,
      isMulti: isMulti,
      components: {
        Option: ValueOption
      },
      maxMenuHeight: maxMenuHeight || undefined
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
      error: error
    }), value !== null && value !== void 0 && value.description ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DescriptionContainer, {
      ref: ref,
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
        reposition: false,
        padding: 20,
        align: "center",
        isOpen: hovering,
        positions: ['bottom'],
        content: /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_PopoverTemplate, {
          children: value === null || value === void 0 ? void 0 : value.description
        }),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_Icon, {
          src: info_icon
        })
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var Select = (TDMSelect);
// CONCATENATED MODULE: ./src/utils/react-to-angular.js
// eslint-disable-next-line @typescript-eslint/no-var-requires,no-undef
var React = __webpack_require__(1);
// eslint-disable-next-line @typescript-eslint/no-var-requires,no-undef
var ReactDOM = __webpack_require__(30);
function ReactToAngularJS(Component, directiveName, angularApp, bindings) {
  bindings = bindings || {};
  if (typeof window === "undefined" || typeof angularApp === "undefined") return;
  angularApp.directive(directiveName, function () {
    return {
      scope: bindings,
      replace: true,
      link: function link(scope, element) {
        // Add $scope
        scope.$scope = scope;
        // First render - needed?
        ReactDOM.render(React.createElement(Component, scope), element[0]);

        // Watch for any changes in bindings, then rerender
        var keys = [];
        for (var _i = 0, _Object$keys = Object.keys(bindings); _i < _Object$keys.length; _i++) {
          var bindingKey = _Object$keys[_i];
          if (bindings[bindingKey] !== "&") {
            keys.push(bindingKey);
          }
        }
        //debugger;
        scope.$watchGroup(keys, function () {
          ReactDOM.render(React.createElement(Component, scope), element[0]);
        });
        scope.$on("$destroy", function handler() {
          // destruction code here
          ReactDOM.unmountComponentAtNode(element[0]);
        });
      }
    };
  });
}
function getService(serviceName) {
  if (typeof window === "undefined" || typeof window.angular === "undefined" || "production" === 'development') {
    return undefined;
  }
  return window.angular.element(document.body).injector().get(serviceName);
}

/* harmony default export */ var react_to_angular = (ReactToAngularJS);
// CONCATENATED MODULE: ./src/apis/example.ts
var exampleAPIs = {
  getTableVersions: {
    result: [{
      "task_name": "aaaa",
      "task_description": "",
      "executed_by": "[tahata@k2view.com##[k2view_k2v_user]](mailto:tahata@k2view.com )",
      "execution_datetime": "2024-02-13 07:46:01.232883",
      "task_execution_id": 1,
      "number_of_records": 10
    }, {
      "task_name": "aaaa",
      "task_description": "",
      "executed_by": "[tahata@k2view.com##[k2view_k2v_user]](mailto:tahata@k2view.com )",
      "execution_datetime": "2024-02-13 07:46:01.232883",
      "task_execution_id": 2,
      "number_of_records": 10
    }, {
      "task_name": "aaaa",
      "task_description": "",
      "executed_by": "[tahata@k2view.com##[k2view_k2v_user]](mailto:tahata@k2view.com )",
      "execution_datetime": "2024-02-13 07:46:01.232883",
      "task_execution_id": 3,
      "number_of_records": 10
    }]
  },
  getTableByBeAndEnv: {
    "result": [{
      "CRM_DB": [{
        "public": [{
          "taskExecutionId": 1449,
          "taskName": "tables 01-05-2024 01:54:38",
          "tableName": "activity"
        }, {
          "tableName": "address"
        }, {
          "tableName": "case_note"
        }, {
          "tableName": "cases"
        }, {
          "tableName": "contract"
        }, {
          "tableName": "customer"
        }]
      }]
    }, {
      "BILLING_DB": [{
        "public": [{
          "taskExecutionId": 1449,
          "taskName": "tables 01-05-2024 01:54:38",
          "tableName": "balance"
        }, {
          "tableName": "contract_offer_mapping"
        }, {
          "tableName": "invoice"
        }, {
          "tableName": "offer"
        }, {
          "tableName": "payment"
        }, {
          "tableName": "subscriber"
        }, {
          "tableName": "case_note"
        }]
      }, {
        "shaischema": [{
          "tableName": "subsubsub"
        }]
      }]
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  getActiveBusinessentities: {
    "result": [{
      "be_id": 1,
      "be_name": "Customer",
      "execution_mode": "VERTICAL"
    }, {
      "be_id": 2,
      "be_name": "Contract",
      "execution_mode": "HORIZONTAL"
    }, {
      "be_id": 3,
      "be_name": "Order",
      "execution_mode": "HORIZONTAL"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  environmentsbyuserandbe: {
    "result": [{
      "synthetic_indicator": 'None',
      "environment_id": 14,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "testtarenv15063",
      "mask_sensitive_data": true,
      "environment_sync_mode": "ON"
    }, {
      "synthetic_indicator": 'None',
      "environment_id": 4,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "TARGET",
      "environment_name": "ENV3",
      "mask_sensitive_data": true,
      "environment_sync_mode": "OFF"
    }, {
      "synthetic_indicator": 'None',
      "environment_id": 1,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "BOTH",
      "environment_name": "ENV1",
      "environment_sync_mode": "FORCE"
    }, {
      "synthetic_indicator": 'RuleBased',
      "environment_id": 9999,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "Synthetic"
    }, {
      "synthetic_indicator": 'AI',
      "environment_id": 10000,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "AI"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  userEnvironments: {
    "result": [{
      "synthetic_indicator": 'None',
      "environment_id": 14,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "testtarenv15063",
      "mask_sensitive_data": true,
      "environment_sync_mode": "ON"
    }, {
      "synthetic_indicator": 'None',
      "environment_id": 4,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "TARGET",
      "environment_name": "ENV3",
      "mask_sensitive_data": true,
      "environment_sync_mode": "OFF"
    }, {
      "synthetic_indicator": 'None',
      "environment_id": 1,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "BOTH",
      "environment_name": "ENV1",
      "environment_sync_mode": "FORCE"
    }, {
      "synthetic_indicator": 'RuleBased',
      "environment_id": 9999,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "Synthetic"
    }, {
      "synthetic_indicator": 'AI',
      "environment_id": 10000,
      "role_id": "admin",
      "assignment_type": "admin",
      "environment_type": "SOURCE",
      "environment_name": "AI"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  getDMPopParams: {
    "result": {
      "dummy312321421412434252352353525": {
        "editor": {
          "schema": {
            "type": "string"
          },
          "name": "dummy",
          "id": "com.k2view.default"
        },
        "default": "BBBBBB",
        "description": "111111111\n d,asfh\n dasfkljdlasf kasdfjldasf jasdfkldjasf adfjkl ,ashfjlgsdghj asdghas asdkjgas   asdghkjdashgas kldshjflidas fdzkjasfghdas zkgfas\d hjdlas gfdassdgasdg ",
        "type": "string",
        "mandatory": false
      },
      "dummy1": {
        "editor": {
          "schema": {
            "type": "string"
          },
          "name": "dummy1",
          "id": "com.k2view.default"
        },
        "default": "EEEEE",
        "description": "",
        "type": "string",
        "mandatory": false
      },
      "ACTOR": {
        "editor": {
          "name": "ACTOR",
          "schema": {},
          "id": "com.k2view.mTableKey"
        },
        "default": null,
        "description": "",
        "type": "any",
        "mandatory": false
      },
      "index": {
        "editor": {
          "name": "index",
          "schema": {
            "type": "boolean"
          },
          "syncOutput": true,
          "id": "com.k2view.default"
        },
        "default": null,
        "description": "",
        "type": "integer",
        "mandatory": true,
        value: false,
        order: 2
      },
      "message": {
        "editor": {
          "schema": {
            "type": "string"
          },
          "name": "message",
          "id": "com.k2view.default"
        },
        "default": "AAAAAAA",
        "description": "",
        "type": "string",
        "mandatory": false
      },
      "distribution": {
        "editor": {
          "name": "distribution",
          "schema": {},
          "id": "com.k2view.distribution"
        },
        "default": null,
        "description": "",
        "type": "any",
        "mandatory": false
      },
      "TYPE": {
        "editor": {
          "name": "TYPE",
          "schema": {},
          "id": "com.k2view.mTableKey"
        },
        "default": null,
        "description": "",
        "type": "any",
        "mandatory": false
      },
      "DATE_EXP": {
        "editor": {
          "name": "DATE_EXP",
          "schema": {
            "type": "date"
          },
          "syncOutput": true,
          "id": "com.k2view.default"
        },
        value: "2023-03-24T16:20",
        "default": null,
        "description": "Demo Date",
        "type": "date",
        "mandatory": false,
        order: 1
      }
    },
    "errorCode": "SUCCESS",
    "message": null
  },
  "tasks/getTrainingModels": {
    "result": [{
      "name": "training 10000 customers",
      "task_execution_id": 16,
      "execution_time": "2023-08-07 14:28:43.559",
      "executed_by": "admin",
      "execution_note": 'note 1',
      "num_of_entities": 20
    }, {
      "name": "training 1M patients",
      "task_execution_id": 17,
      "execution_time": "2023-08-07 14:28:43.559",
      "executed_by": "admin",
      "execution_note": 'note 2',
      "num_of_entities": 35
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  getcustomlogicflows: {
    "result": [{
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicCity2",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicInv",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicInvDate",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicStates",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicCityBool1",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicCityMulti",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicCitySQL",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "newCusLogicCitySQL2",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "DirectCustomLogicCity",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "mtableCusLogicCity",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "DirectCustomLogicCity2",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "DirectCustomLogicCity3",
      "Description": ""
    }, {
      "luName": "PATIENT_LU",
      "flowName": "CustomLogicSql1",
      "Description": ""
    }, {
      "luName": "",
      "flowName": "CustomLogicSql",
      "Description": "Generic Custom Logic Flow for Sqls"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  getCustomLogicParams: {
    "result": [{
      "editor": {
        "name": "ObjectAA",
        "schema": {
          "type": "object",
          "properties": {}
        },
        "context": {
          "ObjectAA": {
            "self": "value"
          }
        },
        "syncOutput": true,
        "id": "com.k2view.default",
        "mandatory": true
      },
      "default": null,
      "description": "A constant value",
      "type": "object",
      "mandatory": true
    }, {
      "editor": {
        "name": "BoolTest",
        "schema": {
          "type": "boolean"
        },
        "context": {
          "BoolTest": {
            "self": "value"
          },
          "value": {
            "const": true
          }
        },
        "syncOutput": true,
        "id": "com.k2view.default",
        "mandatory": true
      },
      "default": true,
      "description": "A constant value",
      "type": "bool",
      "mandatory": true
    }, {
      "editor": {
        "name": "date",
        "schema": {
          "type": "date"
        },
        "context": {
          "date": {
            "self": "value"
          },
          "value": {
            "const": "2023-07-26 11:18:23.955"
          }
        },
        "syncOutput": true,
        "id": "com.k2view.default",
        "mandatory": true
      },
      "default": "2023-07-26 11:18:23.955",
      "description": "A constant value",
      "type": "date",
      "mandatory": true
    }, {
      "editor": {
        "name": "arr123",
        "schema": {
          "type": "array",
          "items": {}
        },
        "context": {
          "arr123": {
            "self": "value"
          }
        },
        "syncOutput": true,
        "id": "com.k2view.default",
        "mandatory": false
      },
      "default": null,
      "description": "A constant value",
      "type": "array",
      "mandatory": false
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "businessentity/1/preexecutionprocess": {
    "result": [{
      "process_id": 2,
      "be_id": 1,
      "process_name": "preTaskExePrintToLog",
      "process_type": "pre",
      "process_description": "pre2",
      "execution_order": 1
    }, {
      "process_id": 3,
      "be_id": 1,
      "process_name": "preTaskExePrintToLog3",
      "process_type": "pre",
      "process_description": "pre2",
      "execution_order": 2
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "businessentity/1/postexecutionprocess": {
    "result": [{
      "process_id": 1,
      "be_id": 1,
      "process_name": "LoggerFlow2",
      "process_description": null,
      "execution_order": 2
    }, {
      "process_id": 2,
      "be_id": 1,
      "process_name": "LoggerFlow3",
      "process_description": null,
      "execution_order": 1
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "task/255/globals": {
    "result": [{
      "global_name": "CLONE_CLEANUP_RETENTION_PERIOD_TYPE",
      "lu_name": "PATIENT_VISITS",
      "task_id": 255,
      "global_value": "Minutes1"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "environment/getAllGlobals": {
    "result": [{
      "globalName": "BASE_PATH",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "CLONE_CLEANUP_RETENTION_PERIOD_TYPE",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "Minutes"
      }]
    }, {
      "globalName": "CLONE_CLEANUP_RETENTION_PERIOD_VALUE",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "3"
      }]
    }, {
      "globalName": "DEVELOPMENT_PRODUCT_VERSION",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "DEV"
      }]
    }, {
      "globalName": "EXTRACT_MASKING_FLAG",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "false"
      }]
    }, {
      "globalName": "INSTANCES_RANDOM_MAX",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "INSTANCES_RANDOM_MIN",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "LOAD_MASKING_FLAG",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "false"
      }]
    }, {
      "globalName": "MAIL_ADDRESS",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "MASK_FLAG",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "0"
      }, {
        "luName": "PATIENT_LU",
        "defaultValue": "10"
      }]
    }, {
      "globalName": "ORACLE8_DB_TYPE",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "Oracle8"
      }]
    }, {
      "globalName": "PRODUCTION_PRODUCT_VERSION",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "PROD"
      }]
    }, {
      "globalName": "REF_KEYSPACE_NAME",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "k2view_tdm"
      }]
    }, {
      "globalName": "REFERENCE_LU",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "TDM_Reference"
      }]
    }, {
      "globalName": "ROWS_GENERATOR",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "false"
      }]
    }, {
      "globalName": "SHAIGLOB1",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "SHAIGLOB2",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "SHAIGLOB3",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "SYNTHETIC_INDICATOR",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "false"
      }]
    }, {
      "globalName": "TAHAGLOB",
      "Description": "",
      "luList": [{
        "luName": "PATIENT_LU",
        "defaultValue": ""
      }]
    }, {
      "globalName": "TDM_DEL_TABLE_PREFIX",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "TAR"
      }]
    }, {
      "globalName": "TDM_REF_UPD_SIZE",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "10000"
      }]
    }, {
      "globalName": "TDM_SYN_ENV_ID",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "TDM_SYN_ENV_NAME",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": ""
      }]
    }, {
      "globalName": "TDM_SYNTHETIC_DATA",
      "Description": "",
      "luList": [{
        "luName": "ALL",
        "defaultValue": "false"
      }]
    }, {
      "globalName": "TEST112",
      "Description": "",
      "luList": [{
        "luName": "PATIENT_VISITS",
        "defaultValue": ""
      }]
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  retentionperiodinfo: {
    "result": {
      "reservationDefaultPeriod": {
        "units": "Days",
        "value": 5
      },
      "retentionDefaultPeriod": {
        "units": "Do Not Delete",
        "value": -1
      },
      "maxRetentionPeriodForTesters": {
        "units": "Days",
        "value": 90
      },
      "reservationPeriodTypes": [{
        "name": "Minutes",
        "units": 0.00069444444
      }, {
        "name": "Hours",
        "units": 0.04166666666
      }, {
        "name": "Days",
        "units": 1
      }, {
        "name": "Weeks",
        "units": 7
      }, {
        "name": "Years",
        "units": 365
      }],
      "versioningRetentionPeriodForTesters": {
        "units": "Days",
        "value": 5,
        "allow_doNotDelete": false
      },
      "versioningRetentionPeriod": {
        "units": "Days",
        "value": 5,
        "allow_doNotDelete": true
      },
      "retentionPeriodTypes": [{
        "name": "Minutes",
        "units": 0.00069444444
      }, {
        "name": "Hours",
        "units": 0.04166666666
      }, {
        "name": "Days",
        "units": 1
      }, {
        "name": "Weeks",
        "units": 7
      }, {
        "name": "Years",
        "units": 365
      }],
      "maxReservationPeriodForTesters": {
        "units": "Days",
        "value": 90
      }
    },
    "errorCode": "SUCCESS",
    "message": ""
  },
  "task/1/logicalunits": {
    "result": [{
      "lu_name": "PATIENT_LU",
      "lu_id": 1,
      "task_id": 265
    }, {
      "lu_name": "PATIENT_VISITS",
      "lu_id": 15,
      "task_id": 265
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "task/getReferenceTaskTable": {
    "result": [{
      "be_name": "Customer",
      "reference_table_name": "PATIENT_REF",
      "count_indicator": "true",
      "truncate_indicator": "false",
      "table_pk_list": "",
      "logical_unit_name": "PATIENT_LU",
      "interface_name": "HIS_DB",
      "count_flow": "",
      "target_schema_name": "TDM_TARGET",
      "target_interface_name": "TARGET_DB",
      "schema_name": "TDM_SOURCE",
      "target_ref_table_name": "PATIENT_REF"
    }, {
      "be_name": "Contract",
      "reference_table_name": "REF_GIBRISH",
      "count_indicator": "true",
      "truncate_indicator": "false",
      "table_pk_list": "",
      "logical_unit_name": "PATIENT_LU",
      "interface_name": "HIS_DB",
      "count_flow": "",
      "target_schema_name": "TDM_TARGET",
      "target_interface_name": "TARGET_DB",
      "schema_name": "TDM_SOURCE",
      "target_ref_table_name": "REF_GIBRISH"
    }, {
      "be_name": "Order",
      "reference_table_name": "VISIT_REF",
      "count_indicator": "true",
      "truncate_indicator": "false",
      "table_pk_list": "",
      "logical_unit_name": "PATIENT_VISITS",
      "interface_name": "HIS_DB",
      "count_flow": "",
      "target_schema_name": "TDM_TARGET",
      "target_interface_name": "TARGET_DB",
      "schema_name": "TDM_SOURCE",
      "target_ref_table_name": "VISIT_REF"
    }, {
      "be_name": "Contract",
      "reference_table_name": "cas_gibrish",
      "count_indicator": "true",
      "truncate_indicator": "true",
      "table_pk_list": "",
      "logical_unit_name": "PATIENT_LU",
      "interface_name": "DB_CASSANDRA",
      "count_flow": "ref100",
      "target_schema_name": "k2view_tdm",
      "target_interface_name": "DB_CASSANDRA",
      "schema_name": "k2view_tdm",
      "target_ref_table_name": "cas_gibrish_tar"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "businessentity/1/environment/1/logicalunits": {
    result: [{
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU",
      "lu_id": 1,
      "product_name": "PROD",
      "value": 1,
      "label": "PATIENT_LU"
    }, {
      "lu_parent_name": "PATIENT_LU",
      "lu_name": "PATIENT_VISITS",
      "lu_id": 15,
      "product_name": "PROD",
      "value": 15,
      "label": "PATIENT_VISITS"
    }, {
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU_2",
      "lu_id": 2,
      "product_name": "PROD2",
      "value": 2,
      "label": "PATIENT_LU_2"
    }, {
      "lu_parent_name": "PATIENT_LU_2",
      "lu_name": "PATIENT_VISITS_2",
      "lu_id": 16,
      "product_name": "PROD2",
      "value": 16,
      "label": "PATIENT_VISITS_2"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "businessentity/1/logicalunits": {
    result: [{
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU",
      "lu_id": 1,
      "product_name": "PROD",
      "value": 1,
      "label": "PATIENT_LU"
    }, {
      "lu_parent_name": "PATIENT_LU",
      "lu_name": "PATIENT_VISITS",
      "lu_id": 15,
      "product_name": "PROD",
      "value": 15,
      "label": "PATIENT_VISITS"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "tasks/getGenerationModels": {
    "result": [{
      "execution_note": null,
      "start_execution_time": "2024-04-21 08:53:58.706141",
      "number_of_entities": 20,
      "fabric_execution_id": "ba0b5043-74c8-488e-a978-5189c1a042bc",
      "task_execution_id": 1669,
      "num_of_succeeded_entities": 20,
      "task_id": 5,
      "creation_date": "2024-04-21 08:52:47.16",
      "num_of_failed_entities": 0,
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU",
      "task_title": "Customer 21-04-2024 11:51:57",
      "row_number": 1,
      "root_indicator": "Y",
      "task_executed_by": "sivan.mulla@k2view.com"
    }, {
      "execution_note": null,
      "start_execution_time": "2024-04-21 09:22:51.167731",
      "number_of_entities": 10,
      "fabric_execution_id": "349f3c2f-aed4-4301-8e74-fa253c3f06d4",
      "task_execution_id": 1672,
      "num_of_succeeded_entities": 10,
      "task_id": 8,
      "creation_date": "2024-04-21 09:21:37.471",
      "num_of_failed_entities": 0,
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU",
      "task_title": "Customer 21-04-2024 12:21:18",
      "row_number": 1,
      "root_indicator": "Y",
      "task_executed_by": "sivan.mulla@k2view.com"
    }, {
      "execution_note": null,
      "start_execution_time": "2024-04-21 09:28:09.737754",
      "number_of_entities": 10,
      "fabric_execution_id": "bcb24b4f-643a-430d-8173-475d3db30028",
      "task_execution_id": 1675,
      "num_of_succeeded_entities": 10,
      "task_id": 8,
      "creation_date": "2024-04-21 09:26:59.927",
      "num_of_failed_entities": 0,
      "lu_parent_name": null,
      "lu_name": "PATIENT_LU",
      "task_title": "Customer 21-04-2024 12:21:18",
      "row_number": 2,
      "root_indicator": "Y",
      "task_executed_by": "sivan.mulla@k2view.com"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "businessentity/1/sourceEnv/ENV1/parameters": {
    "result": {
      "CUSTOMER.NOTE_DATE": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.NOTE_DATE",
        "PARAM_TYPE": "DATETIME",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": [" ", "2015-09-17 11:25:14.0", "2016-11-18 00:08:03.0", "2016-07-13 16:40:59.0"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "BILLING.VIP_STATUS": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.VIP_STATUS",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Silver", "Gold", "Platinum"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "CUSTOMER.CASE_STATUS": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CASE_STATUS",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Closed"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "BILLING.SUBSCRIBER_TYPE": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.SUBSCRIBER_TYPE",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["1", "3", "4"],
        "MIN_VALUE": "1",
        "MAX_VALUE": "4",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "CUSTOMER.CASE_TYPE": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CASE_TYPE",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["Billing Issue"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CONTRACT_START_DATE": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CONTRACT_START_DATE",
        "PARAM_TYPE": "DATETIME",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["2017-02-20 01:43:59.0", "2015-01-12 13:28:41.0", "2015-01-16 21:06:11.0", "2016-04-16 15:20:58.0", "2015-08-16 12:53:44.0"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "BILLING.TOTAL_PAYMENT_AMOUNT": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.TOTAL_PAYMENT_AMOUNT",
        "PARAM_TYPE": "INTEGER",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["1216", "3416", "1356", "2230", "1416"],
        "MIN_VALUE": "1216",
        "MAX_VALUE": "3416",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.NO_OF_OPEN_INVOICES": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.NO_OF_OPEN_INVOICES",
        "PARAM_TYPE": "INTEGER",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["0", "1", "2", "3"],
        "MIN_VALUE": "0",
        "MAX_VALUE": "3",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.OFFER_START_DATE": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.OFFER_START_DATE",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["2015-01-16 21:39:16.000", "2015-11-21 01:51:35.000", "2016-09-06 08:58:18.000", "2016-02-24 09:00:40.000"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "CUSTOMER.STATE": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.STATE",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["KY"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "BILLING.TOTAL_BALANCE_AMOUNT": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.TOTAL_BALANCE_AMOUNT",
        "PARAM_TYPE": "INTEGER",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["2830", "1244", "4662", "3971", "4513"],
        "MIN_VALUE": "1244",
        "MAX_VALUE": "4662",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "CUSTOMER.ACTIVTY_DATE": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.ACTIVTY_DATE",
        "PARAM_TYPE": "DATETIME",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["2016-03-17 16:16:47.0"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.NO_OF_OPEN_CASES": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.NO_OF_OPEN_CASES",
        "PARAM_TYPE": "INTEGER",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": null,
        "MIN_VALUE": "0",
        "MAX_VALUE": "0",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CONTRACT_DESCRIPTION": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CONTRACT_DESCRIPTION",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Unlimited call", "450 min", "Roaming special", "5G tether", "10G 3G"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CITY": {
        "BE_ID": "2",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CITY",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Pittsburg"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "BILLING.OFFER_DESCRIPTION": {
        "BE_ID": "2",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.OFFER_DESCRIPTION",
        "PARAM_TYPE": "TEXT",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Unlimited call", "450 min", "Roaming special", "10G 3G"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      }
    },
    "errorCode": "SUCCESS"
  },
  "businessentity/2/sourceEnv/ENV1/analysiscount": {
    "result": 100,
    "errorCode": "SUCCESS"
  },
  "tasks/versionsForLoad": {
    "result": {
      "EntityReservationValidations": {},
      "ListOfVersions": [{
        "number_of_extracted_entities": 3,
        "execution_note": null,
        "version_no": 3,
        "version_type": "Selected Entities",
        "fabric_execution_id": "72f3b088-215f-48ae-8261-73cfc009bac9",
        "task_execution_id": 1674,
        "num_of_succeeded_entities": 3,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 09:22:24.011",
        "version_name": "version",
        "lu_name": "PATIENT_LU",
        "root_indicator": "Y"
      }, {
        "number_of_extracted_entities": 15,
        "execution_note": null,
        "version_no": 3,
        "version_type": "Selected Entities",
        "fabric_execution_id": "77538bdc-b6fd-46a9-8480-b21a32e2f68a",
        "task_execution_id": 1674,
        "num_of_succeeded_entities": 15,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 09:22:24.011",
        "version_name": "version",
        "lu_name": "PATIENT_VISITS",
        "root_indicator": "N"
      }, {
        "number_of_extracted_entities": 3,
        "execution_note": null,
        "version_no": 2,
        "version_type": "Selected Entities",
        "fabric_execution_id": "08c9380f-2b10-4037-9791-fdb24b83a7af",
        "task_execution_id": 1673,
        "num_of_succeeded_entities": 3,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 09:21:42.099",
        "version_name": "version",
        "lu_name": "PATIENT_LU",
        "root_indicator": "Y"
      }, {
        "number_of_extracted_entities": 15,
        "execution_note": null,
        "version_no": 2,
        "version_type": "Selected Entities",
        "fabric_execution_id": "6cedb892-3c57-4983-a657-f41092930de9",
        "task_execution_id": 1673,
        "num_of_succeeded_entities": 15,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 09:21:42.099",
        "version_name": "version",
        "lu_name": "PATIENT_VISITS",
        "root_indicator": "N"
      }, {
        "number_of_extracted_entities": 3,
        "execution_note": null,
        "version_no": 1,
        "version_type": "Selected Entities",
        "fabric_execution_id": "ce876bf5-2ad7-40b1-a9b4-264b1f9fc429",
        "task_execution_id": 1670,
        "num_of_succeeded_entities": 3,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 08:56:49.188",
        "version_name": "version",
        "lu_name": "PATIENT_LU",
        "root_indicator": "Y"
      }, {
        "number_of_extracted_entities": 15,
        "execution_note": null,
        "version_no": 1,
        "version_type": "Selected Entities",
        "fabric_execution_id": "3125c8c6-1ae7-4c4f-9293-d1f13ed38ad1",
        "task_execution_id": 1670,
        "num_of_succeeded_entities": 15,
        "task_id": 6,
        "task_last_updated_by": "sivan.mulla@k2view.com",
        "num_of_failed_entities": 0,
        "version_datetime": "2024-04-21 08:56:49.188",
        "version_name": "version",
        "lu_name": "PATIENT_VISITS",
        "root_indicator": "N"
      }]
    },
    "errorCode": "SUCCESS",
    "message": null
  },
  "task/255/preexecutionprocess": {
    "result": [{
      "process_id": 2,
      "process_name": "preTaskExePrintToLog",
      "task_id": 255,
      "process_type": "pre",
      "execution_order": 1
    }, {
      "process_id": 3,
      "process_name": "preTaskExePrintToLog3",
      "task_id": 255,
      "process_type": "pre",
      "execution_order": 2
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "getTableFields": {
    "result": [{
      "column_name": "customer_id",
      "column_type": "NUMBER"
    }, {
      "column_name": "activity_id",
      "column_type": "NUMBER"
    }, {
      "column_name": "activity_date",
      "column_type": "TEXT"
    }, {
      "column_name": "activity_note",
      "column_type": "TEXT"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "wsGetFabricRolesByUser": {
    "result": ["testerRole", "Everybody"],
    "errorCode": "SUCCESS",
    "message": null
  },
  "wsGetParamsAutoWidth": {
    "result": "false",
    "errorCode": "SUCCESS",
    "message": null
  },
  "wsGetParamsLUName": {
    "result": "true",
    "errorCode": "SUCCESS",
    "message": null
  },
  "getExecutionProcessParams": {
    "result": [{
      "process_name": "LoggerFlow2",
      "editors": [{
        "editor": {
          "template": true,
          "schema": {
            "type": "string"
          },
          "name": "sql",
          "context": {
            "batch": {
              "const": false
            },
            "interface": {
              "const": "CRM_DB"
            },
            "sql": {
              "self": "sql"
            }
          },
          "language": "sql",
          "id": "com.k2view.code",
          "mandatory": true
        },
        "default": null,
        "description": "The SQL statement to perform.\nCan contain either ordered params using ? or named params using ${} notation.",
        "type": "string",
        "mandatory": true,
        "value": "10"
      }, {
        "editor": {
          "name": "SQLParams",
          "schema": {
            "type": "string"
          },
          "context": {
            "SQLParams": {
              "self": "value"
            }
          },
          "syncOutput": true,
          "id": "com.k2view.default",
          "mandatory": false
        },
        "default": null,
        "description": "Optional parameters for the select query. You can set multiple input parameters separated by a comma.",
        "type": "string",
        "mandatory": false,
        "value": "10"
      }]
    }, {
      "process_name": "LoggerFlow3",
      "editors": [{
        "editor": {
          "template": true,
          "schema": {
            "type": "string"
          },
          "name": "sql",
          "context": {
            "batch": {
              "const": false
            },
            "interface": {
              "const": "CRM_DB"
            },
            "sql": {
              "self": "sql"
            }
          },
          "language": "sql",
          "id": "com.k2view.code",
          "mandatory": true,
          "value": "10"
        },
        "default": null,
        "description": "The SQL statement to perform.\nCan contain either ordered params using ? or named params using ${} notation.",
        "type": "string",
        "mandatory": true
      }, {
        "editor": {
          "name": "SQLParams",
          "schema": {
            "type": "string"
          },
          "context": {
            "SQLParams": {
              "self": "value"
            }
          },
          "syncOutput": true,
          "id": "com.k2view.default",
          "mandatory": false
        },
        "default": null,
        "description": "Optional parameters for the select query. You can set multiple input parameters separated by a comma.",
        "type": "string",
        "mandatory": false
      }]
    }],
    "errorCode": "SUCCESS",
    "message": null
  }
};
// CONCATENATED MODULE: ./src/apis/task.ts




var runningRequests = {};
var task_toastr = getService('toastr');
var task_fetchData = /*#__PURE__*/function () {
  var _ref = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee(path, body, method) {
    var response;
    return regenerator_default.a.wrap(function _callee$(_context) {
      while (1) switch (_context.prev = _context.next) {
        case 0:
          _context.next = 2;
          return window.k2api.invokeFabricWebService(path, body, method);
        case 2:
          response = _context.sent;
          if (!response.isError) {
            _context.next = 5;
            break;
          }
          throw new Error(response.message);
        case 5:
          if (!(response.errorCode === 'FAILED')) {
            _context.next = 10;
            break;
          }
          task_toastr === null || task_toastr === void 0 ? void 0 : task_toastr.error(response.message);
          throw new Error(response.message);
        case 10:
          if (response.errorCode === 'WARNING') {
            task_toastr === null || task_toastr === void 0 ? void 0 : task_toastr.warning(response.message);
          }
        case 11:
          return _context.abrupt("return", response.result);
        case 12:
        case "end":
          return _context.stop();
      }
    }, _callee);
  }));
  return function fetchData(_x, _x2, _x3) {
    return _ref.apply(this, arguments);
  };
}();
var invokeFabricWebService = /*#__PURE__*/function () {
  var _ref2 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee3(path, body, method, force) {
    return regenerator_default.a.wrap(function _callee3$(_context3) {
      while (1) switch (_context3.prev = _context3.next) {
        case 0:
          if (true) {
            _context3.next = 2;
            break;
          }
          return _context3.abrupt("return", JSON.parse(JSON.stringify(exampleAPIs[path].result)));
        case 2:
          if (!(window.k2api && window.k2api.invokeFabricWebService)) {
            _context3.next = 7;
            break;
          }
          if (!force) {
            _context3.next = 5;
            break;
          }
          return _context3.abrupt("return", task_fetchData(path, body, method));
        case 5:
          if (runningRequests[path]) {
            clearTimeout(runningRequests[path]);
          }
          return _context3.abrupt("return", new Promise(function (resolve, reject) {
            runningRequests[path] = setTimeout( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
              var data;
              return regenerator_default.a.wrap(function _callee2$(_context2) {
                while (1) switch (_context2.prev = _context2.next) {
                  case 0:
                    _context2.prev = 0;
                    _context2.next = 3;
                    return task_fetchData(path, body, method);
                  case 3:
                    data = _context2.sent;
                    resolve(data);
                    _context2.next = 10;
                    break;
                  case 7:
                    _context2.prev = 7;
                    _context2.t0 = _context2["catch"](0);
                    reject(_context2.t0);
                  case 10:
                  case "end":
                    return _context2.stop();
                }
              }, _callee2, null, [[0, 7]]);
            })), 100);
          }));
        case 7:
          throw new Error('window.k2api is not defined');
        case 8:
        case "end":
          return _context3.stop();
      }
    }, _callee3);
  }));
  return function invokeFabricWebService(_x4, _x5, _x6, _x7) {
    return _ref2.apply(this, arguments);
  };
}();
var getActiveBEs = /*#__PURE__*/function () {
  var _ref4 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee4() {
    return regenerator_default.a.wrap(function _callee4$(_context4) {
      while (1) switch (_context4.prev = _context4.next) {
        case 0:
          return _context4.abrupt("return", invokeFabricWebService('getActiveBusinessentities', {}, 'GET'));
        case 1:
        case "end":
          return _context4.stop();
      }
    }, _callee4);
  }));
  return function getActiveBEs() {
    return _ref4.apply(this, arguments);
  };
}();
var getEnvironments = /*#__PURE__*/function () {
  var _ref5 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee5(be_name, force) {
    return regenerator_default.a.wrap(function _callee5$(_context5) {
      while (1) switch (_context5.prev = _context5.next) {
        case 0:
          if (!be_name) {
            _context5.next = 2;
            break;
          }
          return _context5.abrupt("return", invokeFabricWebService('userEnvironments', {
            be_name: be_name
          }, 'GET', force));
        case 2:
          return _context5.abrupt("return", invokeFabricWebService('userEnvironments', {}, 'GET', force));
        case 3:
        case "end":
          return _context5.stop();
      }
    }, _callee5);
  }));
  return function getEnvironments(_x8, _x9) {
    return _ref5.apply(this, arguments);
  };
}();
var task_getDataGenerationParams = /*#__PURE__*/function () {
  var _ref6 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee6(taskId, luList) {
    var body;
    return regenerator_default.a.wrap(function _callee6$(_context6) {
      while (1) switch (_context6.prev = _context6.next) {
        case 0:
          body = {};
          if (taskId) {
            body.taskId = taskId;
          }
          if (luList) {
            body.luList = luList;
          }
          return _context6.abrupt("return", invokeFabricWebService('getDMPopParams', body, 'GET'));
        case 4:
        case "end":
          return _context6.stop();
      }
    }, _callee6);
  }));
  return function getDataGenerationParams(_x10, _x11) {
    return _ref6.apply(this, arguments);
  };
}();
var getTrainingModels = /*#__PURE__*/function () {
  var _ref7 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee7(fromDate, toDate, be_name, lu_name) {
    return regenerator_default.a.wrap(function _callee7$(_context7) {
      while (1) switch (_context7.prev = _context7.next) {
        case 0:
          return _context7.abrupt("return", invokeFabricWebService('tasks/getTrainingModels', {
            fromDate: fromDate,
            toDate: toDate,
            be_name: be_name,
            lu_name: lu_name
          }, 'POST'));
        case 1:
        case "end":
          return _context7.stop();
      }
    }, _callee7);
  }));
  return function getTrainingModels(_x12, _x13, _x14, _x15) {
    return _ref7.apply(this, arguments);
  };
}();
var getCustomLogicFlows = /*#__PURE__*/function () {
  var _ref8 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee8(beName, envName) {
    return regenerator_default.a.wrap(function _callee8$(_context8) {
      while (1) switch (_context8.prev = _context8.next) {
        case 0:
          return _context8.abrupt("return", invokeFabricWebService('getcustomlogicflows', {
            beName: beName,
            envName: envName
          }, 'GET'));
        case 1:
        case "end":
          return _context8.stop();
      }
    }, _callee8);
  }));
  return function getCustomLogicFlows(_x16, _x17) {
    return _ref8.apply(this, arguments);
  };
}();
var getCustomLogicParams = /*#__PURE__*/function () {
  var _ref9 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee9(luName, flowName) {
    return regenerator_default.a.wrap(function _callee9$(_context9) {
      while (1) switch (_context9.prev = _context9.next) {
        case 0:
          return _context9.abrupt("return", invokeFabricWebService('getCustomLogicParams', {
            flowName: flowName,
            luName: luName
          }, 'GET'));
        case 1:
        case "end":
          return _context9.stop();
      }
    }, _callee9);
  }));
  return function getCustomLogicParams(_x18, _x19) {
    return _ref9.apply(this, arguments);
  };
}();
var getPreExecutionProcess = /*#__PURE__*/function () {
  var _ref10 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee10(beId) {
    return regenerator_default.a.wrap(function _callee10$(_context10) {
      while (1) switch (_context10.prev = _context10.next) {
        case 0:
          return _context10.abrupt("return", invokeFabricWebService("businessentity/".concat(beId, "/preexecutionprocess"), {}, 'GET'));
        case 1:
        case "end":
          return _context10.stop();
      }
    }, _callee10);
  }));
  return function getPreExecutionProcess(_x20) {
    return _ref10.apply(this, arguments);
  };
}();
var getPostExecutionProcess = /*#__PURE__*/function () {
  var _ref11 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee11(beId) {
    return regenerator_default.a.wrap(function _callee11$(_context11) {
      while (1) switch (_context11.prev = _context11.next) {
        case 0:
          return _context11.abrupt("return", invokeFabricWebService("businessentity/".concat(beId, "/postexecutionprocess"), {}, 'GET'));
        case 1:
        case "end":
          return _context11.stop();
      }
    }, _callee11);
  }));
  return function getPostExecutionProcess(_x21) {
    return _ref11.apply(this, arguments);
  };
}();
var getTaskPostExecutionProcess = /*#__PURE__*/function () {
  var _ref12 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee12(taskId) {
    return regenerator_default.a.wrap(function _callee12$(_context12) {
      while (1) switch (_context12.prev = _context12.next) {
        case 0:
          return _context12.abrupt("return", invokeFabricWebService("task/".concat(taskId, "/postexecutionprocess"), {}, 'GET'));
        case 1:
        case "end":
          return _context12.stop();
      }
    }, _callee12);
  }));
  return function getTaskPostExecutionProcess(_x22) {
    return _ref12.apply(this, arguments);
  };
}();
var getTaskPreExecutionProcess = /*#__PURE__*/function () {
  var _ref13 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee13(taskId) {
    return regenerator_default.a.wrap(function _callee13$(_context13) {
      while (1) switch (_context13.prev = _context13.next) {
        case 0:
          return _context13.abrupt("return", invokeFabricWebService("task/".concat(taskId, "/preexecutionprocess"), {}, 'GET'));
        case 1:
        case "end":
          return _context13.stop();
      }
    }, _callee13);
  }));
  return function getTaskPreExecutionProcess(_x23) {
    return _ref13.apply(this, arguments);
  };
}();
var getTaskTables = /*#__PURE__*/function () {
  var _ref14 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee14(taskId) {
    return regenerator_default.a.wrap(function _callee14$(_context14) {
      while (1) switch (_context14.prev = _context14.next) {
        case 0:
          return _context14.abrupt("return", invokeFabricWebService("task/refsTable/".concat(taskId), {}, 'GET'));
        case 1:
        case "end":
          return _context14.stop();
      }
    }, _callee14);
  }));
  return function getTaskTables(_x24) {
    return _ref14.apply(this, arguments);
  };
}();
var getTaskVariables = /*#__PURE__*/function () {
  var _ref15 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee15(taskId) {
    return regenerator_default.a.wrap(function _callee15$(_context15) {
      while (1) switch (_context15.prev = _context15.next) {
        case 0:
          return _context15.abrupt("return", invokeFabricWebService("task/".concat(taskId, "/globals"), {}, 'GET'));
        case 1:
        case "end":
          return _context15.stop();
      }
    }, _callee15);
  }));
  return function getTaskVariables(_x25) {
    return _ref15.apply(this, arguments);
  };
}();
var getRetentionPeriodsData = /*#__PURE__*/function () {
  var _ref16 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee16() {
    return regenerator_default.a.wrap(function _callee16$(_context16) {
      while (1) switch (_context16.prev = _context16.next) {
        case 0:
          return _context16.abrupt("return", invokeFabricWebService('retentionperiodinfo', {}, 'GET'));
        case 1:
        case "end":
          return _context16.stop();
      }
    }, _callee16);
  }));
  return function getRetentionPeriodsData() {
    return _ref16.apply(this, arguments);
  };
}();
var getReferenceTables = /*#__PURE__*/function () {
  var _ref17 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee17() {
    return regenerator_default.a.wrap(function _callee17$(_context17) {
      while (1) switch (_context17.prev = _context17.next) {
        case 0:
          return _context17.abrupt("return", invokeFabricWebService('task/getReferenceTaskTable', {}, 'POST'));
        case 1:
        case "end":
          return _context17.stop();
      }
    }, _callee17);
  }));
  return function getReferenceTables() {
    return _ref17.apply(this, arguments);
  };
}();
var task_getEnvironmentOwners = /*#__PURE__*/function () {
  var _ref18 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee18(environmentID) {
    return regenerator_default.a.wrap(function _callee18$(_context18) {
      while (1) switch (_context18.prev = _context18.next) {
        case 0:
          return _context18.abrupt("return", invokeFabricWebService("environment/".concat(environmentID, "/owners"), {}, 'GET'));
        case 1:
        case "end":
          return _context18.stop();
      }
    }, _callee18);
  }));
  return function getEnvironmentOwners(_x26) {
    return _ref18.apply(this, arguments);
  };
}();
var getEnvironmentByID = /*#__PURE__*/function () {
  var _ref19 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee19(environmentID) {
    return regenerator_default.a.wrap(function _callee19$(_context19) {
      while (1) switch (_context19.prev = _context19.next) {
        case 0:
          return _context19.abrupt("return", invokeFabricWebService("environment/".concat(environmentID), {}, 'GET'));
        case 1:
        case "end":
          return _context19.stop();
      }
    }, _callee19);
  }));
  return function getEnvironmentByID(_x27) {
    return _ref19.apply(this, arguments);
  };
}();
var getEnvironmentUserRole = /*#__PURE__*/function () {
  var _ref20 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee20(environmentID) {
    return regenerator_default.a.wrap(function _callee20$(_context20) {
      while (1) switch (_context20.prev = _context20.next) {
        case 0:
          return _context20.abrupt("return", invokeFabricWebService("environment/".concat(environmentID, "/userRole"), {}, 'GET'));
        case 1:
        case "end":
          return _context20.stop();
      }
    }, _callee20);
  }));
  return function getEnvironmentUserRole(_x28) {
    return _ref20.apply(this, arguments);
  };
}();
var getFabricRolesByUser = /*#__PURE__*/function () {
  var _ref21 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee21(user) {
    return regenerator_default.a.wrap(function _callee21$(_context21) {
      while (1) switch (_context21.prev = _context21.next) {
        case 0:
          return _context21.abrupt("return", invokeFabricWebService("wsGetFabricRolesByUser", {
            user: user
          }, 'GET'));
        case 1:
        case "end":
          return _context21.stop();
      }
    }, _callee21);
  }));
  return function getFabricRolesByUser(_x29) {
    return _ref21.apply(this, arguments);
  };
}();
var checkAIInstallation = /*#__PURE__*/function () {
  var _ref22 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee22(taskType) {
    return regenerator_default.a.wrap(function _callee22$(_context22) {
      while (1) switch (_context22.prev = _context22.next) {
        case 0:
          return _context22.abrupt("return", invokeFabricWebService("tasks/checkAIInstallation", {
            taskType: taskType
          }, 'POST'));
        case 1:
        case "end":
          return _context22.stop();
      }
    }, _callee22);
  }));
  return function checkAIInstallation(_x30) {
    return _ref22.apply(this, arguments);
  };
}();

// const getTableByEnv = async(source_env: string) => invokeFabricWebService('getTableByEnv', {source_env}, 'POST');
var getTableByBeAndEnv = /*#__PURE__*/function () {
  var _ref23 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee23(source_env, be_name) {
    return regenerator_default.a.wrap(function _callee23$(_context23) {
      while (1) switch (_context23.prev = _context23.next) {
        case 0:
          return _context23.abrupt("return", invokeFabricWebService('getTableByBeAndEnv', {
            source_env: source_env,
            be_name: be_name
          }, 'POST'));
        case 1:
        case "end":
          return _context23.stop();
      }
    }, _callee23);
  }));
  return function getTableByBeAndEnv(_x31, _x32) {
    return _ref23.apply(this, arguments);
  };
}();
var getTableVersions = /*#__PURE__*/function () {
  var _ref24 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee24(table_name, env_name) {
    return regenerator_default.a.wrap(function _callee24$(_context24) {
      while (1) switch (_context24.prev = _context24.next) {
        case 0:
          return _context24.abrupt("return", invokeFabricWebService('getTableVersions', {
            table_name: table_name,
            env_name: env_name
          }, 'POST'));
        case 1:
        case "end":
          return _context24.stop();
      }
    }, _callee24);
  }));
  return function getTableVersions(_x33, _x34) {
    return _ref24.apply(this, arguments);
  };
}();
var getGlobalVariables = /*#__PURE__*/function () {
  var _ref25 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee25(lus) {
    return regenerator_default.a.wrap(function _callee25$(_context25) {
      while (1) switch (_context25.prev = _context25.next) {
        case 0:
          return _context25.abrupt("return", invokeFabricWebService('environment/getAllGlobals', {
            lus: lus
          }, 'GET'));
        case 1:
        case "end":
          return _context25.stop();
      }
    }, _callee25);
  }));
  return function getGlobalVariables(_x35) {
    return _ref25.apply(this, arguments);
  };
}();
var validateReservedEntitiesList = /*#__PURE__*/function () {
  var _ref26 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee26(beID, envID, listOfEntities, filterout_reserved) {
    return regenerator_default.a.wrap(function _callee26$(_context26) {
      while (1) switch (_context26.prev = _context26.next) {
        case 0:
          return _context26.abrupt("return", invokeFabricWebService('validateReservedEntitiesList', {
            beID: beID,
            envID: envID,
            listOfEntities: listOfEntities,
            filterout_reserved: filterout_reserved
          }, 'POST'));
        case 1:
        case "end":
          return _context26.stop();
      }
    }, _callee26);
  }));
  return function validateReservedEntitiesList(_x36, _x37, _x38, _x39) {
    return _ref26.apply(this, arguments);
  };
}();
var task_getLogicalUnits = /*#__PURE__*/function () {
  var _ref27 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee27(be_id, env_id) {
    return regenerator_default.a.wrap(function _callee27$(_context27) {
      while (1) switch (_context27.prev = _context27.next) {
        case 0:
          if (!env_id) {
            _context27.next = 2;
            break;
          }
          return _context27.abrupt("return", invokeFabricWebService("businessentity/".concat(be_id, "/environment/").concat(env_id, "/logicalunits"), {}, 'GET'));
        case 2:
          return _context27.abrupt("return", invokeFabricWebService("businessentity/".concat(be_id, "/logicalunits"), {}, 'GET'));
        case 3:
        case "end":
          return _context27.stop();
      }
    }, _callee27);
  }));
  return function getLogicalUnits(_x40, _x41) {
    return _ref27.apply(this, arguments);
  };
}();
var getTaskLogicalUnits = /*#__PURE__*/function () {
  var _ref28 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee28(task_id) {
    return regenerator_default.a.wrap(function _callee28$(_context28) {
      while (1) switch (_context28.prev = _context28.next) {
        case 0:
          return _context28.abrupt("return", invokeFabricWebService("task/".concat(task_id, "/logicalunits"), {}, 'GET'));
        case 1:
        case "end":
          return _context28.stop();
      }
    }, _callee28);
  }));
  return function getTaskLogicalUnits(_x42) {
    return _ref28.apply(this, arguments);
  };
}();
var getParameters = /*#__PURE__*/function () {
  var _ref29 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee29(be_id, envName) {
    return regenerator_default.a.wrap(function _callee29$(_context29) {
      while (1) switch (_context29.prev = _context29.next) {
        case 0:
          return _context29.abrupt("return", invokeFabricWebService("businessentity/".concat(be_id, "/sourceEnv/").concat(envName, "/parameters"), {}, 'GET'));
        case 1:
        case "end":
          return _context29.stop();
      }
    }, _callee29);
  }));
  return function getParameters(_x43, _x44) {
    return _ref29.apply(this, arguments);
  };
}();
var getEnableParamWidth = /*#__PURE__*/function () {
  var _ref30 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee30() {
    return regenerator_default.a.wrap(function _callee30$(_context30) {
      while (1) switch (_context30.prev = _context30.next) {
        case 0:
          return _context30.abrupt("return", invokeFabricWebService("wsGetParamsAutoWidth", {}, 'GET'));
        case 1:
        case "end":
          return _context30.stop();
      }
    }, _callee30);
  }));
  return function getEnableParamWidth() {
    return _ref30.apply(this, arguments);
  };
}();
var getParamsLUName = /*#__PURE__*/function () {
  var _ref31 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee31() {
    return regenerator_default.a.wrap(function _callee31$(_context31) {
      while (1) switch (_context31.prev = _context31.next) {
        case 0:
          return _context31.abrupt("return", invokeFabricWebService("wsGetParamsLUName", {}, 'GET'));
        case 1:
        case "end":
          return _context31.stop();
      }
    }, _callee31);
  }));
  return function getParamsLUName() {
    return _ref31.apply(this, arguments);
  };
}();
var getTableParameters = /*#__PURE__*/function () {
  var _ref32 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee32(dbInterfaceName, SchemaName, tableName) {
    return regenerator_default.a.wrap(function _callee32$(_context32) {
      while (1) switch (_context32.prev = _context32.next) {
        case 0:
          return _context32.abrupt("return", invokeFabricWebService("getTableFields", {
            tableName: tableName,
            SchemaName: SchemaName,
            dbInterfaceName: dbInterfaceName
          }, 'POST'));
        case 1:
        case "end":
          return _context32.stop();
      }
    }, _callee32);
  }));
  return function getTableParameters(_x45, _x46, _x47) {
    return _ref32.apply(this, arguments);
  };
}();
var getEntitiesCount = /*#__PURE__*/function () {
  var _ref33 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee33(be_id, envName, body) {
    return regenerator_default.a.wrap(function _callee33$(_context33) {
      while (1) switch (_context33.prev = _context33.next) {
        case 0:
          return _context33.abrupt("return", invokeFabricWebService("businessentity/".concat(be_id, "/sourceEnv/").concat(envName, "/analysiscount"), body, 'POST'));
        case 1:
        case "end":
          return _context33.stop();
      }
    }, _callee33);
  }));
  return function getEntitiesCount(_x48, _x49, _x50) {
    return _ref33.apply(this, arguments);
  };
}();
var task_deleteTask = /*#__PURE__*/function () {
  var _ref34 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee34(task_id, task_title) {
    return regenerator_default.a.wrap(function _callee34$(_context34) {
      while (1) switch (_context34.prev = _context34.next) {
        case 0:
          return _context34.abrupt("return", invokeFabricWebService("task/".concat(task_id, "/taskname/").concat(task_title), {}, 'DELETE'));
        case 1:
        case "end":
          return _context34.stop();
      }
    }, _callee34);
  }));
  return function deleteTask(_x51, _x52) {
    return _ref34.apply(this, arguments);
  };
}();
var getExecutionProcessParams = /*#__PURE__*/function () {
  var _ref35 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee35(processType, processesList) {
    return regenerator_default.a.wrap(function _callee35$(_context35) {
      while (1) switch (_context35.prev = _context35.next) {
        case 0:
          return _context35.abrupt("return", invokeFabricWebService("getExecutionProcessParams", {
            processType: processType,
            processesList: processesList
          }, 'POST'));
        case 1:
        case "end":
          return _context35.stop();
      }
    }, _callee35);
  }));
  return function getExecutionProcessParams(_x53, _x54) {
    return _ref35.apply(this, arguments);
  };
}();
var getCheckIfParamsCoupling = /*#__PURE__*/function () {
  var _ref36 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee36() {
    return regenerator_default.a.wrap(function _callee36$(_context36) {
      while (1) switch (_context36.prev = _context36.next) {
        case 0:
          return _context36.abrupt("return", invokeFabricWebService("wsCheckIfParamsCoupling", {}, 'GET'));
        case 1:
        case "end":
          return _context36.stop();
      }
    }, _callee36);
  }));
  return function getCheckIfParamsCoupling() {
    return _ref36.apply(this, arguments);
  };
}();
var getTaskLuEditForTesters = /*#__PURE__*/function () {
  var _ref37 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee37() {
    return regenerator_default.a.wrap(function _callee37$(_context37) {
      while (1) switch (_context37.prev = _context37.next) {
        case 0:
          return _context37.abrupt("return", invokeFabricWebService("wsGetTaskLuEditForTesters", {}, 'GET'));
        case 1:
        case "end":
          return _context37.stop();
      }
    }, _callee37);
  }));
  return function getTaskLuEditForTesters() {
    return _ref37.apply(this, arguments);
  };
}();
var saveTaskAPI = /*#__PURE__*/function () {
  var _ref38 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee38(taskData) {
    return regenerator_default.a.wrap(function _callee38$(_context38) {
      while (1) switch (_context38.prev = _context38.next) {
        case 0:
          if (!taskData.task_id) {
            _context38.next = 2;
            break;
          }
          return _context38.abrupt("return", invokeFabricWebService("task/".concat(taskData.task_id), taskData, 'PUT'));
        case 2:
          return _context38.abrupt("return", invokeFabricWebService('task', taskData, 'POST'));
        case 3:
        case "end":
          return _context38.stop();
      }
    }, _callee38);
  }));
  return function saveTaskAPI(_x55) {
    return _ref38.apply(this, arguments);
  };
}();
var getVersionsForLoad = /*#__PURE__*/function () {
  var _ref39 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee39(fromDate, toDate, entitiesList, lu_list, source_env_name, target_env_name, be_id, filterout_reserved) {
    return regenerator_default.a.wrap(function _callee39$(_context39) {
      while (1) switch (_context39.prev = _context39.next) {
        case 0:
          return _context39.abrupt("return", invokeFabricWebService('tasks/versionsForLoad', {
            fromDate: fromDate,
            toDate: toDate,
            entitiesList: entitiesList,
            lu_list: lu_list,
            source_env_name: source_env_name,
            target_env_name: target_env_name,
            be_id: be_id,
            filterout_reserved: filterout_reserved
          }, 'POST'));
        case 1:
        case "end":
          return _context39.stop();
      }
    }, _callee39);
  }));
  return function getVersionsForLoad(_x56, _x57, _x58, _x59, _x60, _x61, _x62, _x63) {
    return _ref39.apply(this, arguments);
  };
}();
var getGenerationExecutions = /*#__PURE__*/function () {
  var _ref40 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee40(fromDate, toDate, envName, beID, selectedLogicalUnits) {
    return regenerator_default.a.wrap(function _callee40$(_context40) {
      while (1) switch (_context40.prev = _context40.next) {
        case 0:
          return _context40.abrupt("return", invokeFabricWebService('tasks/getGenerationModels', {
            fromDate: fromDate,
            toDate: toDate,
            envName: envName,
            beID: beID,
            selectedLogicalUnits: selectedLogicalUnits
          }, 'POST'));
        case 1:
        case "end":
          return _context40.stop();
      }
    }, _callee40);
  }));
  return function getGenerationExecutions(_x64, _x65, _x66, _x67, _x68) {
    return _ref40.apply(this, arguments);
  };
}();
var taskAPIs = {
  getActiveBEs: getActiveBEs,
  getEnvironments: getEnvironments,
  getDataGenerationParams: task_getDataGenerationParams,
  getTrainingModels: getTrainingModels,
  getCustomLogicFlows: getCustomLogicFlows,
  getCustomLogicParams: getCustomLogicParams,
  getPreExecutionProcess: getPreExecutionProcess,
  getPostExecutionProcess: getPostExecutionProcess,
  getTaskVariables: getTaskVariables,
  getRetentionPeriodsData: getRetentionPeriodsData,
  getVersionsForLoad: getVersionsForLoad,
  getReferenceTables: getReferenceTables,
  getLogicalUnits: task_getLogicalUnits,
  getGenerationExecutions: getGenerationExecutions,
  getParameters: getParameters,
  getEnableParamWidth: getEnableParamWidth,
  getEntitiesCount: getEntitiesCount,
  saveTaskAPI: saveTaskAPI,
  deleteTask: task_deleteTask,
  getTaskLogicalUnits: getTaskLogicalUnits,
  getTableByBeAndEnv: getTableByBeAndEnv,
  getTableVersions: getTableVersions,
  getGlobalVariables: getGlobalVariables,
  getTaskPostExecutionProcess: getTaskPostExecutionProcess,
  getTaskPreExecutionProcess: getTaskPreExecutionProcess,
  getTableParameters: getTableParameters,
  getTaskTables: getTaskTables,
  getEnvironmentOwners: task_getEnvironmentOwners,
  getFabricRolesByUser: getFabricRolesByUser,
  getEnvironmentUserRole: getEnvironmentUserRole,
  getEnvironmentByID: getEnvironmentByID,
  checkAIInstallation: checkAIInstallation,
  validateReservedEntitiesList: validateReservedEntitiesList,
  getExecutionProcessParams: getExecutionProcessParams,
  getParamsLUName: getParamsLUName,
  getCheckIfParamsCoupling: getCheckIfParamsCoupling,
  getTaskLuEditForTesters: getTaskLuEditForTesters
};
/* harmony default export */ var apis_task = (taskAPIs);
// CONCATENATED MODULE: ./src/components/task/AdvancedBE/styles.ts

var AdvancedBE_styles_templateObject, AdvancedBE_styles_templateObject2, AdvancedBE_styles_templateObject3, AdvancedBE_styles_templateObject4, AdvancedBE_styles_templateObject5, AdvancedBE_styles_templateObject6, AdvancedBE_styles_templateObject7, AdvancedBE_styles_templateObject8, AdvancedBE_styles_templateObject9, AdvancedBE_styles_templateObject10, AdvancedBE_styles_templateObject11, AdvancedBE_styles_templateObject12, AdvancedBE_styles_templateObject13, AdvancedBE_styles_templateObject14, AdvancedBE_styles_templateObject15;

var AdvancedBE_styles_Container = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject || (AdvancedBE_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    margin-top: 15px;\n    position: relative;\n"])));
var LogicalUnitsContainer = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject2 || (AdvancedBE_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 400px;\n    position: relative;\n    z-index: 100;\n    max-height: 350px;\n    overflow-y: auto;\n    overflow-x: hidden;\n    padding: 19px 0px 30px 0px;\n    object-fit: contain;\n    border-radius: 6px;\n    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);\n    background-color: #fff;\n"])));
var AdvancedBE_styles_Title = styled_components_browser_esm["b" /* default */].span(AdvancedBE_styles_templateObject3 || (AdvancedBE_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.43;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    cursor: pointer;\n"])));
var LogicalUnitTitle = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject4 || (AdvancedBE_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 18px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.33;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    position: relative;\n    margin: 0px 20px;\n    margin-bottom: 19px;\n"])));
var LogicalUnitBody = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject5 || (AdvancedBE_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    margin: 10px 25px 0px 30px;\n"])));
var styles_Seprator = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject6 || (AdvancedBE_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    border: solid 1px #ccc;\n"])));
var CloseIcon = styled_components_browser_esm["b" /* default */].img(AdvancedBE_styles_templateObject7 || (AdvancedBE_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    right: 0px;\n    top: 5px;\n    cursor: pointer;\n"])));
var AdvancedBE_styles_Icon = styled_components_browser_esm["b" /* default */].img(AdvancedBE_styles_templateObject8 || (AdvancedBE_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var Actions = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject9 || (AdvancedBE_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    display: flex;\n    margin-top:5px;\n    align-items: center;\n    justify-content: flex-end;\n    gap: 18px;\n    border-bottom: ", ";\n    padding-bottom: 13px;\n"])), function (props) {
  return props.border ? '1px solid #ccc' : '';
});
var ActionItem = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject10 || (AdvancedBE_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    cursor: pointer;\n"])));
var SystemHeader = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject11 || (AdvancedBE_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    padding: 10px 0px;\n    border-bottom: 1px solid #ccc;\n    justify-content: space-between;\n    padding-right: 11px;\n"])));
var SystemsContainer = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject12 || (AdvancedBE_styles_templateObject12 = taggedTemplateLiteral_default()(["\n\n"])));
var SystemBody = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject13 || (AdvancedBE_styles_templateObject13 = taggedTemplateLiteral_default()(["\n    padding: 13px 10px 15px 37px;\n    background-color: #f2f2f2;\n    display: flex;\n    gap: 12px;\n    flex-direction: column;\n    border-bottom: solid 1px #ccc;\n"])));
var LogicalUnitContainer = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject14 || (AdvancedBE_styles_templateObject14 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    \n"])));
var ExecutionModeContainer = styled_components_browser_esm["b" /* default */].div(AdvancedBE_styles_templateObject15 || (AdvancedBE_styles_templateObject15 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    align-items: flex-start;\n    border-bottom: 1px solid #ccc;\n    gap: 10px;\n    margin-top: 10px;\n    padding-bottom: 10px;\n"])));
// CONCATENATED MODULE: ./src/images/xclose.svg
/* harmony default export */ var xclose = ("js/dist/f7db0cd4cd00f1f6bb2346432c313b0b.svg");
// CONCATENATED MODULE: ./src/images/arrow-up.svg
/* harmony default export */ var arrow_up = ("js/dist/97a5e48592ebbcad3ed51ba0739a7eba.svg");
// CONCATENATED MODULE: ./src/images/arrow-down.svg
/* harmony default export */ var arrow_down = ("js/dist/e98a6f4185c19c2d8e0ebcb5b207d97e.svg");
// CONCATENATED MODULE: ./src/components/Tabs/styles.ts

var Tabs_styles_templateObject, Tabs_styles_templateObject2, Tabs_styles_templateObject3, Tabs_styles_templateObject4, Tabs_styles_templateObject5, Tabs_styles_templateObject6, Tabs_styles_templateObject7;

var Tabs_styles_Container = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject || (Tabs_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    position: relative;\n    font-size: 16px;\n"])));
var styles_TabTitle = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject2 || (Tabs_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    position: relative;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: ", ";\n"])), function (props) {
  return props.changed ? '#1483f3' : '#2e2e2e';
});
var styles_SelectedTab = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject3 || (Tabs_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    height: 2px;\n    width: 100%;\n    background-color: #1483f3;\n"])));
var styles_TabItem = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject4 || (Tabs_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    display: flex;\n    align-items: center;\n    gap: 13px;\n    cursor: pointer;\n"])));
var Tabs_styles_Icon = styled_components_browser_esm["b" /* default */].img(Tabs_styles_templateObject5 || (Tabs_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    margin-left: 5px;\n    height: 15px;\n    margin-bottom: 3px;\n"])));
var Tabs_styles_Body = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject6 || (Tabs_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
var Header = styled_components_browser_esm["b" /* default */].div(Tabs_styles_templateObject7 || (Tabs_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    gap: 48px;\n    padding-bottom: 28px;\n"])));
// CONCATENATED MODULE: ./src/components/Tabs/index.tsx





function Tabs(props) {
  var tabs = props.tabs,
    selected = props.selected,
    setSelectedTab = props.setSelectedTab,
    children = props.children,
    changedTabs = props.changedTabs;
  var getTab = Object(react["useCallback"])(function (tabData) {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TabItem, {
      onClick: function onClick() {
        return setSelectedTab(tabData.name);
      },
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TabTitle, {
        changed: (changedTabs || []).indexOf(tabData.name) >= 0,
        children: [tabData.name, (changedTabs || []).indexOf(tabData.name) >= 0 && tabData.icon ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Tabs_styles_Icon, {
          src: tabData.icon
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), selected === tabData.name ? /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_SelectedTab, {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })
    });
  }, [selected, setSelectedTab, changedTabs]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Tabs_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Header, {
      children: tabs.map(function (it) {
        return getTab(it);
      })
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tabs_styles_Body, {
      children: children
    })]
  });
}
/* harmony default export */ var components_Tabs = (Tabs);
// CONCATENATED MODULE: ./src/components/task/AdvancedBE/index.tsx

















function AdvancedBE() {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    register = _useContext.register,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    allLogicalUnits = _useContext.allLogicalUnits;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    data = _useState2[0],
    setData = _useState2[1];
  var _useState3 = Object(react["useState"])([]),
    _useState4 = slicedToArray_default()(_useState3, 2),
    openedSystems = _useState4[0],
    setOpenedSystems = _useState4[1];
  var selected_logical_units = taskData.selected_logical_units,
    execution_mode = taskData.execution_mode,
    dataSourceType = taskData.dataSourceType,
    source_type = taskData.source_type,
    enable_advanced_for_testers = taskData.enable_advanced_for_testers;
  var _useState5 = Object(react["useState"])(false),
    _useState6 = slicedToArray_default()(_useState5, 2),
    open = _useState6[0],
    setOpen = _useState6[1];
  var _useState7 = Object(react["useState"])(''),
    _useState8 = slicedToArray_default()(_useState7, 2),
    error = _useState8[0],
    setError = _useState8[1];
  var _useState9 = Object(react["useState"])(''),
    _useState10 = slicedToArray_default()(_useState9, 2),
    localExecutionMode = _useState10[0],
    setLocalExecutionMode = _useState10[1];
  var ref = Object(react["useRef"])();
  var _useState11 = Object(react["useState"])('Systems & Logical units'),
    _useState12 = slicedToArray_default()(_useState11, 2),
    selectedTab = _useState12[0],
    setSelectedTab = _useState12[1];
  var AuthService = getService('AuthService');
  var role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
  Object(react["useEffect"])(function () {
    if (execution_mode) {
      setLocalExecutionMode(execution_mode);
    }
  }, [execution_mode]);
  Object(react["useEffect"])(function () {
    if (open) {
      setError('');
      setSelectedTab('Systems & Logical units');
      setLocalExecutionMode(execution_mode || '');
    }
  }, [open]);
  Object(react["useEffect"])(function () {
    if (!allLogicalUnits || allLogicalUnits.length === 0 || !open) {
      return;
    }
    var newData = [];
    allLogicalUnits.forEach(function (logicalunitItem) {
      var systemFound = newData.find(function (systemItem) {
        return systemItem.system === logicalunitItem.product_name;
      });
      if (systemFound) {
        systemFound.logicalUnits.push({
          selected: (selected_logical_units || []).indexOf(logicalunitItem.lu_id) >= 0,
          lu_name: logicalunitItem.lu_name,
          lu_id: logicalunitItem.lu_id,
          lu_parent_name: logicalunitItem.lu_parent_name
        });
      } else {
        newData.push({
          system: logicalunitItem.product_name,
          selected: false,
          logicalUnits: [{
            selected: (selected_logical_units || []).indexOf(logicalunitItem.lu_id) >= 0,
            lu_name: logicalunitItem.lu_name,
            lu_id: logicalunitItem.lu_id,
            lu_parent_name: logicalunitItem.lu_parent_name
          }]
        });
      }
    });
    newData.forEach(function (it) {
      var selectedLus = it.logicalUnits.filter(function (it) {
        return it.selected;
      });
      if (selectedLus.length > 0) {
        it.selected = true;
      }
    });
    setData(newData);
  }, [allLogicalUnits, selected_logical_units, open]);
  var systemClick = Object(react["useCallback"])(function (system) {
    setOpenedSystems(function (prevSystems) {
      if (prevSystems.indexOf(system) >= 0) {
        return prevSystems.filter(function (it) {
          return it !== system;
        });
      } else {
        return [].concat(toConsumableArray_default()(prevSystems), [system]);
      }
    });
  }, [setOpenedSystems]);

  // const logicalUnitToggle = useCallback((lu_id: number, lu_name: string) => {
  //     const lu_ids = selected_logical_units || [];
  //     const lu_names = selected_logical_units_names || [];
  //     if (lu_ids.indexOf(lu_id) >= 0) {
  //         saveForm({
  //             selected_logical_units: lu_ids.filter((it: number) => lu_id !== it),
  //             selected_logical_units_names: lu_names.filter((it: string) => lu_name !== it),
  //         });
  //     } else {
  //         saveForm({
  //             selected_logical_units: [...lu_ids, lu_id],
  //             selected_logical_units_names: [...lu_names, lu_name],
  //         });
  //     }
  // }, [selected_logical_units, selected_logical_units_names, saveForm]);

  var logicalUnitToggle = Object(react["useCallback"])(function (lu_id, system) {
    setData(function (prevData) {
      var newData = JSON.parse(JSON.stringify(prevData));
      var found = newData.find(function (it) {
        return it.system === system;
      });
      if (found) {
        var luFound = found.logicalUnits.find(function (it) {
          return it.lu_id === lu_id;
        });
        if (luFound) {
          luFound.selected = !luFound.selected;
        }
        var selectedLus = found.logicalUnits.filter(function (it) {
          return it.selected;
        });
        if (selectedLus.length > 0) {
          found.selected = true;
        } else {
          found.selected = false;
        }
      }
      return newData;
    });
  }, [setData]);
  var systemToggle = Object(react["useCallback"])(function (system) {
    setData(function (prevData) {
      var newData = JSON.parse(JSON.stringify(prevData));
      var found = newData.find(function (it) {
        return it.system === system;
      });
      if (found) {
        found.selected = !found.selected;
        found.logicalUnits.forEach(function (it) {
          it.selected = found.selected;
        });
      }
      return newData;
    });
  }, [setData]);
  var allAction = Object(react["useCallback"])(function (flag) {
    setData(function (prevData) {
      var newData = JSON.parse(JSON.stringify(prevData));
      newData.forEach(function (it) {
        it.selected = flag;
        it.logicalUnits.forEach(function (it) {
          it.selected = flag;
        });
      });
      return newData;
    });
  }, [setData]);
  var isSelectedLU = function isSelectedLU(lu_name, selectedLus) {
    return selectedLus.findIndex(function (it) {
      return it.lu_name === lu_name;
    }) >= 0;
  };
  var checkGap = function checkGap(lu, selectedLus) {
    if (lu.lu_parent_name && !isSelectedLU(lu.lu_parent_name, selectedLus)) {
      /**
       * If a logical unit has a parent and it has not being selected
       * then this is the only chance that we might have a gap.
       * The gap will occur if the lu parent name that is missing
       * has a parent which is not missing. This will generate a gap.
       */

      var luParent = allLogicalUnits.find(function (it) {
        return it.lu_name === lu.lu_parent_name;
      });
      if (luParent) {
        if (luParent.lu_parent_name && isSelectedLU(luParent.lu_parent_name, selectedLus)) {
          return luParent.lu_name;
        }
      }
    }
    return '';
  };
  var checkIfRootIsMissing = function checkIfRootIsMissing(selectedLus) {
    var missingRootLU = [];

    // check if there is lu which has a parent that does not have a parent (root)
    selectedLus.forEach(function (lu) {
      if (lu.lu_parent_name) {
        var luParent = allLogicalUnits.find(function (it) {
          return it.lu_name === lu.lu_parent_name;
        });
        if (luParent) {
          // if lu has a parent that does not have a parent which is missing then root is missing
          if (!luParent.lu_parent_name && !isSelectedLU(luParent.lu_name, selectedLus)) {
            missingRootLU.push(luParent.lu_name);
          }
        }
      }
    });
    return missingRootLU;
  };
  var SaveData = Object(react["useCallback"])(function () {
    var selectedLus = [];
    data.forEach(function (it) {
      it.logicalUnits.forEach(function (it) {
        if (it.selected) {
          selectedLus.push(it);
        }
      });
    });
    if (selectedLus.length === 0) {
      setError('Please choose at least one logical unit');
      return;
    }
    var luGap = '';
    selectedLus.forEach(function (lu) {
      if (luGap) {
        return;
      }
      luGap = checkGap(lu, selectedLus);
    });
    if (luGap) {
      setError("There is a gap in the LU hierarchy. Add ".concat(luGap, " to complete the gap."));
      return;
    }
    var missingRoot = checkIfRootIsMissing(selectedLus);
    if (missingRoot.length > 0) {
      setError("Root LU missing, Please add ".concat(missingRoot.join(', '), " ."));
      return;
    }
    saveForm({
      selected_logical_units: selectedLus.map(function (it) {
        return it.lu_id;
      }),
      selected_logical_units_names: selectedLus.map(function (it) {
        return it.lu_name;
      }),
      execution_mode: localExecutionMode
    });
    setOpen(false);
  }, [data, saveForm, setError, localExecutionMode]);
  var tabs = Object(react["useMemo"])(function () {
    if ("".concat(dataSourceType, "_").concat(source_type) !== 'data_source_BE') {
      return [{
        name: 'Systems & Logical units'
      }];
    }
    return [{
      name: 'Systems & Logical units'
    }, {
      name: 'Execution Mode'
    }];
  }, []);
  var getSelectedTab = Object(react["useCallback"])(function () {
    if (selectedTab === 'Systems & Logical units') {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Actions, {
          border: true,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: function onClick() {
              return allAction(false);
            },
            children: "Clear all"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: function onClick() {
              return allAction(true);
            },
            children: "Add all"
          })]
        }), data.map(function (it) {
          return /*#__PURE__*/Object(jsx_runtime["jsxs"])(SystemsContainer, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(SystemHeader, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                name: "system_".concat(it.system),
                title: it.system,
                onChange: function onChange() {
                  systemToggle(it.system);
                },
                disabled: role && role.type === 'tester' && !enable_advanced_for_testers,
                value: it.selected
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedBE_styles_Icon, {
                onClick: function onClick() {
                  return systemClick(it.system);
                },
                src: openedSystems.indexOf(it.system) >= 0 ? arrow_up : arrow_down
              })]
            }), openedSystems.indexOf(it.system) >= 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(SystemBody, {
              children: it.logicalUnits.map(function (luItem) {
                return /*#__PURE__*/Object(jsx_runtime["jsx"])(LogicalUnitContainer, {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                    name: "logical_unit_".concat(luItem.lu_name, "}"),
                    title: luItem.lu_name,
                    onChange: function onChange() {
                      logicalUnitToggle(luItem.lu_id, it.system);
                    },
                    disabled: role && role.type === 'tester' && !enable_advanced_for_testers,
                    value: luItem.selected
                  })
                });
              })
            }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
          });
        })]
      });
    } else if (selectedTab === 'Execution Mode') {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionModeContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Use the task's Business Entity execution mode",
          onChange: function onChange() {
            return setLocalExecutionMode('INHERITED');
          },
          name: "execution_mode",
          value: "INHERITED",
          selectedValue: localExecutionMode,
          title: "Use Business Entity execution mode"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Executes entire hierarchy for each entity ID",
          onChange: function onChange() {
            return setLocalExecutionMode('VERTICAL');
          },
          name: "execution_mode",
          value: "VERTICAL",
          selectedValue: localExecutionMode,
          title: "Vertical execution"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Executes system by system, processing all entities for each system",
          onChange: function onChange() {
            return setLocalExecutionMode('HORIZONTAL');
          },
          name: "execution_mode",
          value: "HORIZONTAL",
          selectedValue: localExecutionMode,
          title: "Horizontal execution"
        })]
      });
    }
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
  }, [selectedTab, setLocalExecutionMode, localExecutionMode, allAction, data, systemClick, openedSystems, logicalUnitToggle, role, enable_advanced_for_testers]);
  var getLogicalUnitTemplate = function getLogicalUnitTemplate() {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(LogicalUnitsContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(LogicalUnitTitle, {
        children: ["Advanced BE", /*#__PURE__*/Object(jsx_runtime["jsx"])(CloseIcon, {
          onClick: function onClick() {
            return setOpen(false);
          },
          src: xclose
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(LogicalUnitBody, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Tabs, {
          tabs: tabs,
          selected: selectedTab,
          changedTabs: [],
          setSelectedTab: setSelectedTab,
          children: getSelectedTab()
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Actions, {
          border: false,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: function onClick() {
              return setOpen(false);
            },
            children: "Cancel"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: function onClick() {
              return SaveData();
            },
            children: "Save"
          })]
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
          relativePosition: true,
          submit: true,
          error: error
        })]
      })]
    });
  };
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedBE_styles_Container, {
    ref: ref,
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
      containerStyle: {
        zIndex: '100'
      },
      reposition: false,
      padding: 100,
      align: "end",
      isOpen: open,
      positions: ['right'],
      content: getLogicalUnitTemplate(),
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedBE_styles_Title, {
        onClick: function onClick() {
          return setOpen(!open);
        },
        children: "Advanced"
      })
    })
  });
}
/* harmony default export */ var task_AdvancedBE = (AdvancedBE);
// CONCATENATED MODULE: ./src/components/task/DataMovmentSettings/index.tsx



function DataMovmentSettings_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function DataMovmentSettings_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? DataMovmentSettings_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : DataMovmentSettings_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }












var tabTypes = ['be', 'tables'];
var tabNames = {
  be: 'Business Entity',
  tables: 'Tables'
};
var tabIcons = {
  be: entity_icon,
  tables: table_icon
};
function DataMovmentSettings(props) {
  var _errors$be_name;
  var type = props.type,
    enabledTabs = props.enabledTabs;
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    register = _useContext.register,
    errors = _useContext.errors,
    statusesFuncMap = _useContext.statusesFuncMap;
  var tableList = taskData.tableList,
    be_id = taskData.be_id,
    be_type = taskData.be_type;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    beData = _useState2[0],
    setBeData = _useState2[1];
  var _useState3 = Object(react["useState"])(),
    _useState4 = slicedToArray_default()(_useState3, 2),
    selectedTab = _useState4[0],
    setSelectedTab = _useState4[1];
  var _useState5 = Object(react["useState"])(false),
    _useState6 = slicedToArray_default()(_useState5, 2),
    loading = _useState6[0],
    setLoading = _useState6[1];
  var _useState7 = Object(react["useState"])(),
    _useState8 = slicedToArray_default()(_useState7, 2),
    selectedBe = _useState8[0],
    setSelectedBe = _useState8[1];
  var _useState9 = Object(react["useState"])([]),
    _useState10 = slicedToArray_default()(_useState9, 2),
    tabs = _useState10[0],
    setTabs = _useState10[1];
  var _useState11 = Object(react["useState"])(null),
    _useState12 = slicedToArray_default()(_useState11, 2),
    confirmOpen = _useState12[0],
    setConfirmOpen = _useState12[1];
  Object(react["useEffect"])(function () {
    function fetchData() {
      return _fetchData.apply(this, arguments);
    }
    function _fetchData() {
      _fetchData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getActiveBEs();
            case 3:
              data = _context.sent;
              data.forEach(function (item) {
                item.value = item.be_id;
                item.label = item.be_name;
              });
              setBeData(data);
              setLoading(false);
              _context.next = 12;
              break;
            case 9:
              _context.prev = 9;
              _context.t0 = _context["catch"](0);
              // use hook toast
              setLoading(false);
            case 12:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 9]]);
      }));
      return _fetchData.apply(this, arguments);
    }
    fetchData();
  }, []);
  Object(react["useEffect"])(function () {
    // debugger;
    if (be_id && beData && beData.length > 0) {
      var found = beData.find(function (it) {
        return it.be_id === be_id;
      });
      if (found) {
        setSelectedBe(found);
      } else {
        saveForm({
          be_id: undefined
        });
      }
    } else if (!be_id) {
      setSelectedBe(null);
    }
  }, [be_id, beData]);
  var beChangeLocal = Object(react["useCallback"])(function (item, choose_option) {
    if (item.be_id === be_id) {
      return;
    }
    setSelectedBe(item);
    var updateData = {
      be_id: item && item.be_id || undefined,
      be_name: item && item.be_name || '',
      selected_logical_units: [],
      selected_logical_units_names: [],
      generation_type: 'all',
      selection_method: 'L',
      selection_param_value: undefined,
      num_of_entities: undefined,
      parameters: undefined,
      be_execution_mode: item.execution_mode
    };
    updateData.environment_id = undefined;
    updateData.environment_name = undefined;
    updateData.source_environment_id = undefined;
    updateData.source_environment_name = undefined;
    if (!be_type) {
      updateData.be_type = type;
    }
    saveForm(updateData);
  }, [saveForm, type, be_id, be_type]);
  Object(react["useEffect"])(function () {
    if (tabs && tabs.length > 0) {
      setSelectedTab(tabs[0]);
    }
  }, [tabs]);
  var getTab = Object(react["useCallback"])(function (tabName) {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TabItem, {
      onClick: function onClick() {
        return setSelectedTab(tabName);
      },
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataMovmentSettings_styles_Icon, {
        src: tabIcons[tabName]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TabTitle, {
        children: [tabNames[tabName], selectedTab === tabName ? /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectedTab, {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })]
    });
  }, [setSelectedTab, selectedTab]);
  Object(react["useEffect"])(function () {
    var filteredTabs = tabTypes.filter(function (it) {
      if (!enabledTabs || enabledTabs.length === 0) {
        return true;
      }
      return enabledTabs.indexOf(it) >= 0;
    });
    setTabs(filteredTabs);
  }, [enabledTabs]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataMovmentSettings_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, DataMovmentSettings_objectSpread(DataMovmentSettings_objectSpread({}, register('be_name', {
        required: 'Please Choose Business Entity'
      })), {}, {
        width: "290px",
        title: 'Select business entity',
        mandatory: true,
        value: selectedBe,
        options: beData,
        loading: loading,
        onChange: beChangeLocal,
        error: (_errors$be_name = errors.be_name) === null || _errors$be_name === void 0 ? void 0 : _errors$be_name.message
      })), /*#__PURE__*/Object(jsx_runtime["jsx"])(task_AdvancedBE, {})]
    })
  });
}
/* harmony default export */ var task_DataMovmentSettings = (DataMovmentSettings);
// CONCATENATED MODULE: ./src/components/DataGenerationParameters/index.tsx

















function DataGenerationParameters(props) {
  var dataGenerationParams = props.dataGenerationParams,
    chosenParams = props.chosenParams,
    updateParams = props.updateParams,
    updateValues = props.updateValues;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var synthetic_type = taskData.synthetic_type,
    be_id = taskData.be_id,
    sourceUserRole = taskData.sourceUserRole;
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    paramsRefData = _useState2[0],
    setParamsRefData = _useState2[1];
  var _useState3 = Object(react["useState"])(''),
    _useState4 = slicedToArray_default()(_useState3, 2),
    paramSelectionName = _useState4[0],
    setParamSelectionName = _useState4[1];
  Object(react["useEffect"])(function () {
    if (!paramsRefData) {
      return;
    }
    console.log(paramsRefData.getValues());
  }, [paramsRefData]);
  var getInfoIcon = function getInfoIcon(data) {
    if (data && data.description) {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TooltipPopover, {
        position: "top",
        align: "start",
        body: /*#__PURE__*/Object(jsx_runtime["jsx"])(PopoverTemplate, {
          children: data.description
        }),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(DataGenerationParameters_styles_Icon, {
          src: info_icon
        })
      });
    }
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(DummyIcon, {});
  };
  var addItem = Object(react["useCallback"])(function (key) {
    updateParams({
      key: key,
      action: 'add'
    });
  }, [updateParams]);
  var removeItem = Object(react["useCallback"])(function (key) {
    updateParams({
      key: key,
      action: 'remove'
    });
  }, [updateParams]);
  var getSelectedIcon = Object(react["useCallback"])(function (data, key) {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
      name: "checkbox_generation_".concat(key),
      title: key,
      disabled: data.mandatory && true || false,
      onChange: function onChange(value) {
        if (value) {
          addItem(key);
        } else {
          removeItem(key);
        }
      },
      value: chosenParams.indexOf(key) >= 0
    });
  }, [chosenParams, removeItem, addItem]);
  var getParamItem = Object(react["useCallback"])(function (key) {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ParamsItem, {
      chosen: chosenParams.indexOf(key) >= 0,
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ParamsItemText, {
        title: key,
        children: getSelectedIcon(dataGenerationParams[key], key)
      }), getInfoIcon(dataGenerationParams[key])]
    });
  }, [dataGenerationParams, chosenParams, getSelectedIcon]);
  var getEditorData = Object(react["useCallback"])(function () {
    if (!dataGenerationParams) {
      return;
    }
    var editor = [];
    chosenParams.forEach(function (key) {
      editor.push(dataGenerationParams[key].editor);
    });
    return editor;
  }, [chosenParams, dataGenerationParams]);
  var editorData = getEditorData();
  var getParamSelectionOptions = Object(react["useCallback"])(function () {
    if (!dataGenerationParams) {
      return [];
    }
    var keys = Object.keys(dataGenerationParams).sort(function (item1, item2) {
      return ('' + item1).localeCompare(item2);
    });
    if (!paramSelectionName) {
      return keys;
    }
    return keys.filter(function (key) {
      return key.indexOf(paramSelectionName) >= 0;
    });
  }, [dataGenerationParams, paramSelectionName]);
  var widgetAPIExist = window && window.k2widgets ? true : false;
  var syntheticTypeChange = Object(react["useCallback"])(function (syntheticType) {
    saveForm({
      synthetic_type: syntheticType
    });
  }, [saveForm]);
  var updateFabricRefInData = Object(react["useCallback"])(function (ref) {
    saveForm({
      widgetRefData: ref
    });
  }, [saveForm]);
  Object(react["useEffect"])(function () {
    if (sourceUserRole && sourceUserRole.userType === 'tester' && !sourceUserRole.allow_read) {
      saveForm({
        synthetic_type: 'generated_data'
      });
    }
  }, [sourceUserRole]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(DataGenerationParameters_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Leftside, {
      hideBorders: !be_id,
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_DataMovmentSettingsContainer, {
        hideBorders: !be_id,
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_DataMovmentSettings, {
          enabledTabs: ['be'],
          type: 'source'
        })
      }), be_id ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_SyntheticEntitiesOptions, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: syntheticTypeChange,
            name: "synthetic_type",
            value: "new_data",
            selectedValue: synthetic_type,
            title: 'Generate new data',
            disabled: sourceUserRole && sourceUserRole.userType === 'tester' && !sourceUserRole.allow_read
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: syntheticTypeChange,
            name: "synthetic_type",
            value: "generated_data",
            selectedValue: synthetic_type,
            title: 'Use generated data in the Test data store'
          })]
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
          children: synthetic_type === 'new_data' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
            width: '290px',
            title: "Number of entities to generate"
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        })]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), synthetic_type === 'new_data' && be_id ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Middle, {
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(ParamsContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
          name: "data_generation_parameters",
          title: 'Data generation parameters',
          mandatory: false,
          value: paramSelectionName,
          onChange: setParamSelectionName || function () {},
          type: InputTypes.text,
          placeholder: "Search..."
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ParamsList, {
          children: getParamSelectionOptions().map(function (key) {
            return getParamItem(key);
          })
        })]
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), be_id ? /*#__PURE__*/Object(jsx_runtime["jsx"])(RightSide, {
      children: synthetic_type === 'new_data' && chosenParams && chosenParams.length > 0 && editorData && editorData.length > 0 ? widgetAPIExist ? /*#__PURE__*/Object(jsx_runtime["jsx"])(fabricWidget, {
        updateValues: updateValues,
        editor: getEditorData(),
        saveRef: updateFabricRefInData
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(DummyImg, {
        src: widgetdemo
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var components_DataGenerationParameters = (DataGenerationParameters);
// CONCATENATED MODULE: ./src/components/SelectTrainingModels/styles.ts

var SelectTrainingModels_styles_templateObject, SelectTrainingModels_styles_templateObject2, SelectTrainingModels_styles_templateObject3, SelectTrainingModels_styles_templateObject4, SelectTrainingModels_styles_templateObject5, SelectTrainingModels_styles_templateObject6, SelectTrainingModels_styles_templateObject7, SelectTrainingModels_styles_templateObject8, SelectTrainingModels_styles_templateObject9;

var SelectTrainingModels_styles_Container = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject || (SelectTrainingModels_styles_templateObject = taggedTemplateLiteral_default()(["\n  display: flex;\n  width: 100%;\n"])));
var SelectTrainingModels_styles_Title = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject2 || (SelectTrainingModels_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n"])));
var DatesContainer = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject3 || (SelectTrainingModels_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  margin-top: 20px;\n  margin-bottom: 38px;\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n  display: flex;\n  align-items: center;\n  gap: 30px;\n"])));
var DateItem = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject4 || (SelectTrainingModels_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  display: flex;\n  align-items: center;\n  gap: 10px;\n"])));
var LeftSide = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject5 || (SelectTrainingModels_styles_templateObject5 = taggedTemplateLiteral_default()(["\n  border-right:  ", ";\n  display: flex;\n  flex-direction: column;\n  gap: 25px;\n  min-width: 350px;\n"])), function (props) {
  return props.hideBorders ? '' : '1px solid #ccc';
});
var styles_RightSide = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject6 || (SelectTrainingModels_styles_templateObject6 = taggedTemplateLiteral_default()(["\n  margin-left: 30px;\n  height: 100% ;\n  width: calc(100% - 350px);\n"])));
var SelectTrainingModels_styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject7 || (SelectTrainingModels_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    border-bottom:  ", ";\n    padding-bottom: 10px;\n"])), function (props) {
  return props.hideBorders ? '' : '1px solid #ccc';
});
var SelectTrainingModels_styles_SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject8 || (SelectTrainingModels_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    align-self: flex-start;\n    display: flex;\n    flex-direction: column;\n    gap: 10px;\n"])));
var LUError = styled_components_browser_esm["b" /* default */].div(SelectTrainingModels_styles_templateObject9 || (SelectTrainingModels_styles_templateObject9 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    margin-top: 20px;\n    margin-bottom: 20px;\n    color: #ed5565;\n"])));
// EXTERNAL MODULE: ./node_modules/moment/moment.js
var moment = __webpack_require__(7);
var moment_default = /*#__PURE__*/__webpack_require__.n(moment);

// EXTERNAL MODULE: ./node_modules/@tanstack/table-core/build/lib/index.esm.js
var lib_index_esm = __webpack_require__(56);

// CONCATENATED MODULE: ./src/components/SelectTrainingModels/hooks/useTable.tsx










var useTable_useTable = function useTable(saveLocalData) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var selected_subset_task_exe_id = taskData.selected_subset_task_exe_id,
    trainingStartDate = taskData.trainingStartDate,
    trainingEndDate = taskData.trainingEndDate,
    be_name = taskData.be_name,
    selected_logical_units_names = taskData.selected_logical_units_names;
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  var _useState = Object(react["useState"])(true),
    _useState2 = slicedToArray_default()(_useState, 2),
    loading = _useState2[0],
    setLoading = _useState2[1];
  var _useState3 = Object(react["useState"])([]),
    _useState4 = slicedToArray_default()(_useState3, 2),
    data = _useState4[0],
    setData = _useState4[1];
  Object(react["useEffect"])(function () {
    function fetchData() {
      return _fetchData.apply(this, arguments);
    }
    function _fetchData() {
      _fetchData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var _data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              if (!(!trainingStartDate || !trainingEndDate || !be_name || (selected_logical_units_names || []).length !== 1)) {
                _context.next = 3;
                break;
              }
              return _context.abrupt("return");
            case 3:
              _context.next = 5;
              return apis_task.getTrainingModels(trainingStartDate, trainingEndDate, be_name, (selected_logical_units_names || [])[0]);
            case 5:
              _data = _context.sent;
              _data.forEach(function (item) {
                item.start_execution_time = moment_default()(item.start_execution_time).format('DD MMM YYYY, HH:mm');
              });
              setData(_data);
              _context.next = 13;
              break;
            case 10:
              _context.prev = 10;
              _context.t0 = _context["catch"](0);
              // use hook toast
              setLoading(false);
            case 13:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 10]]);
      }));
      return _fetchData.apply(this, arguments);
    }
    fetchData();
  }, [trainingStartDate, trainingEndDate, be_name, selected_logical_units_names]);
  var setTrainingModel = Object(react["useCallback"])(function (data) {
    saveLocalData({
      selected_subset_task_exe_id: data.task_execution_id
    });
  }, [saveLocalData]);
  var columns = Object(react["useMemo"])(function () {
    return [{
      id: 'select',
      header: '',
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: function onChange() {
              return setTrainingModel(row.original);
            },
            name: "select_training_model",
            value: row.original.task_execution_id,
            selectedValue: selected_subset_task_exe_id,
            title: ''
          })
        });
      }
    }, columnHelper.accessor(function (row) {
      return row.task_title;
    }, {
      id: 'task_title',
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      },
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Task name"
        });
      }
    }), columnHelper.accessor('task_execution_id', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Task execution id"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('start_execution_time', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Execution time"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('num_of_entities', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Number of entities"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('task_executed_by', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Executed by"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('execution_note', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Execution note"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    })];
  }, [columnHelper, selected_subset_task_exe_id, setTrainingModel]);
  return {
    columns: columns,
    data: data,
    loading: loading
  };
};
/* harmony default export */ var hooks_useTable = (useTable_useTable);
// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/objectWithoutProperties.js
var objectWithoutProperties = __webpack_require__(70);
var objectWithoutProperties_default = /*#__PURE__*/__webpack_require__.n(objectWithoutProperties);

// CONCATENATED MODULE: ./src/components/Table/Filter.tsx


function Filter(_ref) {
  var column = _ref.column,
    table = _ref.table;
  // const firstValue = table
  //   .getPreFilteredRowModel()
  //   .flatRows[0]?.getValue(column.id)

  return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
    title: "",
    type: InputTypes.text,
    onChange: function onChange(value) {
      return column.setFilterValue(function (old) {
        return [value, old === null || old === void 0 ? void 0 : old[1]];
      });
    }
  });
}
/* harmony default export */ var Table_Filter = (Filter);
// CONCATENATED MODULE: ./src/components/Table/styles.ts

var Table_styles_templateObject, Table_styles_templateObject2, Table_styles_templateObject3, Table_styles_templateObject4, Table_styles_templateObject5, Table_styles_templateObject6, Table_styles_templateObject7, Table_styles_templateObject8, Table_styles_templateObject9;

var Table_styles_Container = styled_components_browser_esm["b" /* default */].div(Table_styles_templateObject || (Table_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    height: 100%;\n    padding: 0;\n    overflow-x: auto;\n"])));
var TableContainer = styled_components_browser_esm["b" /* default */].table(Table_styles_templateObject2 || (Table_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    border-spacing: 0.5px;\n    background-color: #ccc;\n"])));
var Thead = styled_components_browser_esm["b" /* default */].thead(Table_styles_templateObject3 || (Table_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    height: 100px;\n"])));
var Tbody = styled_components_browser_esm["b" /* default */].tbody(Table_styles_templateObject4 || (Table_styles_templateObject4 = taggedTemplateLiteral_default()(["\n"])));
var TableRow = styled_components_browser_esm["b" /* default */].tr(Table_styles_templateObject5 || (Table_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    height: 50px;\n"])));
var TableHeaderText = styled_components_browser_esm["b" /* default */].div(Table_styles_templateObject6 || (Table_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    border: 0.5px solid #ccc;\n    height: 50px;\n    background-color: #1483f3;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: 500;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #fff;\n    padding: 0px 10px;\n    display: flex;\n    align-items: center;\n"])));
var TableHeaderFilter = styled_components_browser_esm["b" /* default */].div(Table_styles_templateObject7 || (Table_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    border: 0.5px solid #ccc;\n    height: 50px;\n    background-color: #deebf9;\n    display: flex;\n    justify-content: center;\n    align-items: center;\n    padding: 0px 9px;\n"])));
var TableHeadItem = styled_components_browser_esm["b" /* default */].th(Table_styles_templateObject8 || (Table_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    padding: 0px;\n"])));
var TableRowItem = styled_components_browser_esm["b" /* default */].td(Table_styles_templateObject9 || (Table_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n    background-color: ", ";\n    padding-left: 8px;\n    border: 0.5px solid #ccc;\n"])), function (props) {
  return props.row % 2 === 0 ? '#fff' : '#f2f2f2';
});
// EXTERNAL MODULE: ./node_modules/@tanstack/react-table/build/lib/index.esm.js
var build_lib_index_esm = __webpack_require__(27);

// CONCATENATED MODULE: ./src/components/Table/index.tsx


var _excluded = ["indeterminate", "className"];
function Table_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Table_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Table_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Table_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }







function IndeterminateCheckbox(_ref) {
  var indeterminate = _ref.indeterminate,
    _ref$className = _ref.className,
    className = _ref$className === void 0 ? '' : _ref$className,
    rest = objectWithoutProperties_default()(_ref, _excluded);
  var ref = Object(react["useRef"])(null);
  Object(react["useEffect"])(function () {
    if (typeof indeterminate === 'boolean') {
      ref.current.indeterminate = !rest.checked && indeterminate;
    }
  }, [ref, indeterminate]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("input", Table_objectSpread({
    type: "checkbox",
    ref: ref,
    className: className + ' cursor-pointer'
  }, rest));
}
function Table(props) {
  var data = props.data,
    columns = props.columns,
    isExpandable = props.isExpandable,
    rowSelection = props.rowSelection;
  var table = Object(build_lib_index_esm["b" /* useReactTable */])({
    data: data,
    columns: columns,
    getCoreRowModel: Object(lib_index_esm["c" /* getCoreRowModel */])(),
    getSubRows: function getSubRows(row) {
      return row.subRows;
    },
    getExpandedRowModel: Object(lib_index_esm["d" /* getExpandedRowModel */])(),
    defaultColumn: {
      minSize: 0,
      size: Number.MAX_SAFE_INTEGER,
      maxSize: Number.MAX_SAFE_INTEGER
    },
    enableExpanding: isExpandable,
    state: {
      // rowSelection
    }
  });
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Table_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Thead, {
        children: table.getHeaderGroups().map(function (headerGroup) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(TableRow, {
            children: headerGroup.headers.map(function (header) {
              return /*#__PURE__*/Object(jsx_runtime["jsx"])(TableHeadItem, {
                colSpan: header.colSpan,
                children: header.isPlaceholder ? null : /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
                  children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TableHeaderText, {
                    children: Object(build_lib_index_esm["a" /* flexRender */])(header.column.columnDef.header, header.getContext())
                  }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TableHeaderFilter, {
                    children: header.column.getCanFilter() ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Table_Filter, {
                      column: header.column,
                      table: table
                    }) : null
                  })]
                })
              }, header.id);
            })
          }, headerGroup.id);
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tbody, {
        children: table.getRowModel().rows.map(function (row, index) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(TableRow, {
            children: row.getVisibleCells().map(function (cell) {
              return /*#__PURE__*/Object(jsx_runtime["jsx"])(TableRowItem, {
                row: index,
                children: Object(build_lib_index_esm["a" /* flexRender */])(cell.column.columnDef.cell, cell.getContext())
              }, cell.id);
            })
          }, row.id);
        })
      })]
    })
  });
}
/* harmony default export */ var components_Table = (Table);
// CONCATENATED MODULE: ./src/components/RangeDatePicker/styles.ts

var RangeDatePicker_styles_templateObject, RangeDatePicker_styles_templateObject2, RangeDatePicker_styles_templateObject3, RangeDatePicker_styles_templateObject4;

var RangeDatePicker_styles_Container = styled_components_browser_esm["b" /* default */].div(RangeDatePicker_styles_templateObject || (RangeDatePicker_styles_templateObject = taggedTemplateLiteral_default()(["\n margin-bottom: 38px;\n"])));
var RangeDatePicker_styles_Title = styled_components_browser_esm["b" /* default */].div(RangeDatePicker_styles_templateObject2 || (RangeDatePicker_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n  margin-top: 20px;\n  margin-bottom: 20px;\n"])));
var DateContainer = styled_components_browser_esm["b" /* default */].div(RangeDatePicker_styles_templateObject3 || (RangeDatePicker_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n  display: flex;\n  align-items: center;\n  gap: 30px;\n"])));
var styles_DateItem = styled_components_browser_esm["b" /* default */].div(RangeDatePicker_styles_templateObject4 || (RangeDatePicker_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  display: flex;\n  align-items: center;\n  gap: 10px;\n"])));
// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker.min.js
var react_datepicker_min = __webpack_require__(222);
var react_datepicker_min_default = /*#__PURE__*/__webpack_require__.n(react_datepicker_min);

// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker.css
var react_datepicker = __webpack_require__(269);

// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker-cssmodules.css
var react_datepicker_cssmodules = __webpack_require__(271);

// CONCATENATED MODULE: ./src/components/DatePicker/styles.ts

var DatePicker_styles_templateObject, DatePicker_styles_templateObject2, DatePicker_styles_templateObject3, DatePicker_styles_templateObject4;

var DatePicker_styles_Container = styled_components_browser_esm["b" /* default */].div(DatePicker_styles_templateObject || (DatePicker_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n"])));
var IconContainer = styled_components_browser_esm["b" /* default */].button(DatePicker_styles_templateObject2 || (DatePicker_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 40px;\n    height: 40px;\n    border-radius: 3px;\n    border: solid 1px #ccc;\n    display: flex;\n    justify-content: center;\n    align-items: center;\n    background-color: #fff;\n"])));
var DatePicker_styles_Icon = styled_components_browser_esm["b" /* default */].img(DatePicker_styles_templateObject3 || (DatePicker_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 15px;\n"])));
var CustomDatePickerInput = styled_components_browser_esm["b" /* default */].button(DatePicker_styles_templateObject4 || (DatePicker_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    border-radius: 3px;\n    border: solid 1px #ccc;\n    width: 120px;\n    height: 40px;\n    font-size: 15px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n    padding-left: 11px;\n    background-color: #fff;\n    cursor: pointer;\n\n"])));
// CONCATENATED MODULE: ./src/images/calandar-icon.svg
/* harmony default export */ var calandar_icon = ("js/dist/ddc82450adc11897f7d6cf5354ce7994.svg");
// CONCATENATED MODULE: ./src/components/DatePicker/index.tsx








function TDMDatePicker(props) {
  var date = props.date,
    onChange = props.onChange,
    minDate = props.minDate;
  var CustomInputRef = /*#__PURE__*/Object(react["forwardRef"])(function (_ref, ref) {
    var value = _ref.value,
      onClick = _ref.onClick;
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(CustomDatePickerInput, {
      type: "button",
      onClick: onClick,
      ref: ref,
      children: value
    });
  });
  console.log(minDate);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(DatePicker_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(IconContainer, {
      type: "button",
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(DatePicker_styles_Icon, {
        src: calandar_icon
      })
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(react_datepicker_min_default.a, {
      minDate: minDate || null,
      selected: date,
      onChange: onChange,
      dateFormat: "dd MMM yyyy",
      customInput: /*#__PURE__*/Object(jsx_runtime["jsx"])(CustomInputRef, {})
    })]
  });
}
/* harmony default export */ var DatePicker = (TDMDatePicker);
// CONCATENATED MODULE: ./src/components/RangeDatePicker/index.tsx




function RangeDatePicker(props) {
  var title = props.title,
    startDate = props.startDate,
    endDate = props.endDate,
    startDateChange = props.startDateChange,
    endDateChange = props.endDateChange;
  var startDateUpdate = function startDateUpdate(startDate) {
    if (startDate && endDate && startDate > endDate) {
      endDateChange(new Date(startDate.getTime() + 2592000000));
    }
    startDateChange(startDate);
  };
  var endDateUpdate = function endDateUpdate(endDate) {
    endDateChange(endDate);
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(RangeDatePicker_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RangeDatePicker_styles_Title, {
      children: title
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(DateContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_DateItem, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RangeDatePicker_styles_Title, {
          children: "From date:"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(DatePicker, {
          date: startDate || null,
          onChange: startDateUpdate
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_DateItem, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RangeDatePicker_styles_Title, {
          children: "To date:"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(DatePicker, {
          minDate: startDate,
          date: endDate || null,
          onChange: endDateUpdate
        })]
      })]
    })]
  });
}
/* harmony default export */ var components_RangeDatePicker = (RangeDatePicker);
// CONCATENATED MODULE: ./src/components/SelectTrainingModels/index.tsx
















function SelectTrainingModels(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var trainingStartDate = taskData.trainingStartDate,
    trainingEndDate = taskData.trainingEndDate,
    be_name = taskData.be_name,
    synthetic_type = taskData.synthetic_type,
    selected_logical_units_names = taskData.selected_logical_units_names,
    sourceUserRole = taskData.sourceUserRole,
    disableGeneration = taskData.disableGeneration;
  var _useTable = hooks_useTable(saveForm),
    columns = _useTable.columns,
    data = _useTable.data,
    loading = _useTable.loading;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  Object(react["useEffect"])(function () {
    var updateData = {};
    if (!trainingStartDate) {
      updateData.trainingStartDate = new Date(Date.now() - 2592000000);
    }
    if (!trainingEndDate) {
      updateData.trainingEndDate = new Date();
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
    function fetchCheckAIInstaltion() {
      return _fetchCheckAIInstaltion.apply(this, arguments);
    }
    function _fetchCheckAIInstaltion() {
      _fetchCheckAIInstaltion = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.checkAIInstallation('AIGeneration');
            case 3:
              _context.next = 8;
              break;
            case 5:
              _context.prev = 5;
              _context.t0 = _context["catch"](0);
              saveForm({
                disableGeneration: true
              });
            case 8:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 5]]);
      }));
      return _fetchCheckAIInstaltion.apply(this, arguments);
    }
    if (!disableGeneration) {
      fetchCheckAIInstaltion();
    }
  }, []);
  var startDateUpdate = Object(react["useCallback"])(function (startDate) {
    var updateData = {
      trainingStartDate: startDate
    };
    saveForm(updateData);
  }, [saveForm]);
  var endDateUpdate = Object(react["useCallback"])(function (endDate) {
    saveForm({
      trainingEndDate: endDate
    });
  }, [saveForm]);
  Object(react["useEffect"])(function () {
    if (sourceUserRole && sourceUserRole.userType === 'tester' && !sourceUserRole.allow_read || disableGeneration) {
      saveForm({
        synthetic_type: 'generated_data'
      });
    }
  }, [sourceUserRole, disableGeneration]);
  var syntheticTypeChange = Object(react["useCallback"])(function (syntheticType) {
    saveForm({
      synthetic_type: syntheticType
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(SelectTrainingModels_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(LeftSide, {
      hideBorders: !be_name,
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SelectTrainingModels_styles_DataMovmentSettingsContainer, {
        hideBorders: !be_name,
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_DataMovmentSettings, {
          enabledTabs: ['be'],
          type: 'source'
        })
      }), be_name ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(SelectTrainingModels_styles_SyntheticEntitiesOptions, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: syntheticTypeChange,
            name: "synthetic_type",
            value: "new_data",
            selectedValue: synthetic_type,
            title: 'Generate new data',
            disabled: sourceUserRole && sourceUserRole.userType === 'tester' && !sourceUserRole.allow_read || disableGeneration
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: syntheticTypeChange,
            name: "synthetic_type",
            value: "generated_data",
            selectedValue: synthetic_type,
            title: 'Use generated data in the Test data store'
          })]
        }), be_name && synthetic_type === 'new_data' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
          width: '315px',
          title: "Number of entities"
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), be_name && synthetic_type === 'new_data' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_RightSide, {
      children: (selected_logical_units_names || []).length !== 1 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(LUError, {
        children: "Select only one LU."
      }) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
          title: 'Select data generator',
          startDate: trainingStartDate,
          startDateChange: startDateUpdate,
          endDate: trainingEndDate,
          endDateChange: endDateUpdate
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Table, {
          columns: columns,
          data: data
        })]
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var components_SelectTrainingModels = (SelectTrainingModels);
// CONCATENATED MODULE: ./src/components/EnvironmentSelect/styles.ts

var EnvironmentSelect_styles_templateObject;

var EnvironmentSelect_styles_Container = styled_components_browser_esm["b" /* default */].div(EnvironmentSelect_styles_templateObject || (EnvironmentSelect_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: auto;\n    display: flex;\n    align-items: center;\n    cursor: pointer;\n    gap: 20px;\n    align-items: flex-end;\n    position: relative;\n"])));
// CONCATENATED MODULE: ./src/hooks/useToast.ts


var useToast_useToast = function useToast() {
  var toastr = getService('toastr');
  return Object(react["useMemo"])(function () {
    return {
      success: function success(message, inForm) {
        return toastr === null || toastr === void 0 ? void 0 : toastr.success(message);
      },
      error: function error(message, inForm) {
        return toastr === null || toastr === void 0 ? void 0 : toastr.error(message);
      },
      warning: function warning(message, inForm) {
        return toastr === null || toastr === void 0 ? void 0 : toastr.warning(message, '', {
          containerId: inForm ? 'react-toast-container' : undefined
        });
      }
    };
  }, []);
};
/* harmony default export */ var hooks_useToast = (useToast_useToast);
// CONCATENATED MODULE: ./src/components/EnvironmentSelect/index.tsx










function EnvironmentSelect(props) {
  var be_name = props.be_name,
    environment_id = props.environment_id,
    onChange = props.onChange,
    syntheticType = props.syntheticType,
    isMandatory = props.isMandatory,
    mode = props.mode,
    title = props.title;
  var toast = hooks_useToast();
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData;
  var dataSourceType = taskData.dataSourceType,
    source_type = taskData.source_type;
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    selectedEnviornment = _useState2[0],
    setSelectedEnviornment = _useState2[1];
  var _useState3 = Object(react["useState"])(true),
    _useState4 = slicedToArray_default()(_useState3, 2),
    loading = _useState4[0],
    setLoading = _useState4[1];
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    environments = _useState6[0],
    setEnvironments = _useState6[1];
  Object(react["useEffect"])(function () {
    function fetchData() {
      return _fetchData.apply(this, arguments);
    }
    function _fetchData() {
      _fetchData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data, filteredData;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getEnvironments(be_name);
            case 3:
              data = _context.sent;
              data.forEach(function (item) {
                item.value = item.environment_id;
                item.label = item.environment_name;
              });
              if (mode === 'SOURCE' && dataSourceType === 'data_source' && source_type === 'tables') {
                filteredData = data.filter(function (it) {
                  return it.allowed_refresh_reference_data !== false;
                });
                if (filteredData.length === 0 || filteredData.filter(function (it) {
                  return it.synthetic_indicator === 'None';
                }).length === 0) {
                  toast.warning('You don’t have permissions to run tables');
                }
                setEnvironments(filteredData);
              } else {
                setEnvironments(data);
              }
              setLoading(false);
              _context.next = 12;
              break;
            case 9:
              _context.prev = 9;
              _context.t0 = _context["catch"](0);
              // use hook toast
              setLoading(false);
            case 12:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 9]]);
      }));
      return _fetchData.apply(this, arguments);
    }
    fetchData();
  }, [be_name]);
  Object(react["useEffect"])(function () {
    if (loading) {
      return;
    }
    if (syntheticType !== 'None') {
      if (selectedEnviornment && selectedEnviornment.synthetic_indicator === syntheticType) {
        return;
      }
      var syntheticEnvironment = environments.find(function (it) {
        return it.synthetic_indicator === syntheticType;
      });
      setSelectedEnviornment(syntheticEnvironment || null);
      onChange(syntheticEnvironment || null);
    } else if (selectedEnviornment && selectedEnviornment.synthetic_indicator !== 'None') {
      onChange(null);
    }
  }, [syntheticType, environments, selectedEnviornment, loading]);
  Object(react["useEffect"])(function () {
    if (environment_id === undefined || environment_id === null) {
      setSelectedEnviornment(null);
      return;
    }
    environments.forEach(function (item) {
      item.value = item.environment_id;
      item.label = item.environment_name;
      if (environment_id && environment_id === item.environment_id) {
        setSelectedEnviornment(item);
        // onChange(item);
      }
    });
  }, [environment_id, environments]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(EnvironmentSelect_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
      width: "290px",
      title: title,
      mandatory: isMandatory,
      value: selectedEnviornment,
      options: environments.filter(function (it) {
        return it.synthetic_indicator === 'None' && (it.environment_type === mode || it.environment_type === 'BOTH');
      }),
      loading: loading,
      onChange: onChange
    })
  });
}
/* harmony default export */ var components_EnvironmentSelect = (EnvironmentSelect);
// CONCATENATED MODULE: ./src/images/pii-icon.svg
/* harmony default export */ var pii_icon = ("js/dist/4bace6bde9f0c66715224a842651a702.svg");
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/styles.ts

var ReferenceTables_styles_templateObject, ReferenceTables_styles_templateObject2, ReferenceTables_styles_templateObject3, ReferenceTables_styles_templateObject4, ReferenceTables_styles_templateObject5, ReferenceTables_styles_templateObject6, ReferenceTables_styles_templateObject7, ReferenceTables_styles_templateObject8, ReferenceTables_styles_templateObject9, ReferenceTables_styles_templateObject10, ReferenceTables_styles_templateObject11, ReferenceTables_styles_templateObject12;

var ReferenceTables_styles_Container = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject || (ReferenceTables_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    border-left: 2px solid #ccc;\n    padding-left: 25px;\n    display: flex;\n    flex-direction: column;\n    gap: 17px;\n"])));
var TablesContainer = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject2 || (ReferenceTables_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    gap: 7px;\n    align-items:center;\n    height: 269px;\n"])));
var SourceTablesContainer = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject3 || (ReferenceTables_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 260px;\n    background-color: #f2f2f2;\n    padding: 10px 20px 30px 20px;\n    height: calc(100% - 40px);\n    height: -webkit-fill-available;\n"])));
var SourceTables = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject4 || (ReferenceTables_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    margin-top: 10px;\n    overflow-y: auto;\n    height: calc(100% - 30px);\n"])));
var MoveTablesButton = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject5 || (ReferenceTables_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    width: 27px;\n    height: 27px;\n    display: flex;\n    align-items: center;\n    justify-content: center;\n    cursor: pointer;\n    border-radius: 3px;\n    background-color: #1483f3;\n"])));
var SelectedTables = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject6 || (ReferenceTables_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    flex-grow: 1;\n    height: 269px;\n"])));
var ReferenceTables_styles_Icon = styled_components_browser_esm["b" /* default */].img(ReferenceTables_styles_templateObject7 || (ReferenceTables_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    width: ", ";\n"])), function (props) {
  return props.width || '';
});
var ReferenceTables_styles_Title = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject8 || (ReferenceTables_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n"])));
var EnvIconContainer = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject9 || (ReferenceTables_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 12px;\n    margin-top: 10px;\n    cursor: pointer;\n"])));
var TablesIconContainer = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject10 || (ReferenceTables_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 12px;\n    padding-left: 23px;\n    margin-top: 7px;\n    cursor: pointer;\n"])));
var Tables = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject11 || (ReferenceTables_styles_templateObject11 = taggedTemplateLiteral_default()([" \n    margin-top: 7px;\n"])));
var TableItemContainer = styled_components_browser_esm["b" /* default */].div(ReferenceTables_styles_templateObject12 || (ReferenceTables_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 12px;\n    cursor: pointer;\n    background-color: ", ";\n    padding-left: 55px;  \n"])), function (props) {
  return props.selected ? '#e5e5e5' : '';
});
// CONCATENATED MODULE: ./src/images/env-icon.svg
/* harmony default export */ var env_icon = ("js/dist/7f89285037360b34caf7b2d2a1f8b24e.svg");
// CONCATENATED MODULE: ./src/images/tables-folder-icon.svg
/* harmony default export */ var tables_folder_icon = ("js/dist/35e3caef8acbaf5c22616d0e546dfc6e.svg");
// CONCATENATED MODULE: ./src/images/arrow-right.svg
/* harmony default export */ var arrow_right = ("js/dist/873f030ee47d4d2b143fcec667670d37.svg");
// CONCATENATED MODULE: ./src/components/RegularTable/styles.ts

var RegularTable_styles_templateObject, RegularTable_styles_templateObject2, RegularTable_styles_templateObject3, RegularTable_styles_templateObject4, RegularTable_styles_templateObject5, RegularTable_styles_templateObject6, RegularTable_styles_templateObject7, RegularTable_styles_templateObject8;

var RegularTable_styles_Container = styled_components_browser_esm["b" /* default */].div(RegularTable_styles_templateObject || (RegularTable_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    height: 100%;\n    padding: 0;\n    overflow-x: auto;\n    border: 1px solid #ccc;\n    border-radius: 6px;\n"])));
var styles_TableContainer = styled_components_browser_esm["b" /* default */].table(RegularTable_styles_templateObject2 || (RegularTable_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    max-height: 100%;\n    border-spacing: 0.5px;\n"])));
var styles_Thead = styled_components_browser_esm["b" /* default */].thead(RegularTable_styles_templateObject3 || (RegularTable_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    height: 55px;\n    border-bottom: 1px solid #ccc;\n"])));
var styles_Tbody = styled_components_browser_esm["b" /* default */].tbody(RegularTable_styles_templateObject4 || (RegularTable_styles_templateObject4 = taggedTemplateLiteral_default()(["\n"])));
var styles_TableRow = styled_components_browser_esm["b" /* default */].tr(RegularTable_styles_templateObject5 || (RegularTable_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    height: 34px;\n    background-color: transparent !important;\n"])));
var styles_TableHeaderText = styled_components_browser_esm["b" /* default */].div(RegularTable_styles_templateObject6 || (RegularTable_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    height: 50px;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: 500;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    padding: 0px 10px;\n    display: flex;\n    align-items: center;\n"])));
var styles_TableHeadItem = styled_components_browser_esm["b" /* default */].th(RegularTable_styles_templateObject7 || (RegularTable_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    padding: 0px;\n    border: 0;\n    border-bottom: 1px solid #ccc;\n    background-color: transparent;\n\n"])));
var styles_TableRowItem = styled_components_browser_esm["b" /* default */].td(RegularTable_styles_templateObject8 || (RegularTable_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n    padding-left: 18px;\n    border: 0;\n    border-bottom: 1px solid #ccc;\n"])));
// CONCATENATED MODULE: ./src/components/RegularTable/index.tsx





function RegularTable(props) {
  var data = props.data,
    columns = props.columns,
    isExpandable = props.isExpandable;
  var table = Object(build_lib_index_esm["b" /* useReactTable */])({
    data: data,
    columns: columns,
    getCoreRowModel: Object(lib_index_esm["c" /* getCoreRowModel */])(),
    getSubRows: function getSubRows(row) {
      return row.subRows;
    },
    getExpandedRowModel: Object(lib_index_esm["d" /* getExpandedRowModel */])(),
    defaultColumn: {
      minSize: 0,
      size: Number.MAX_SAFE_INTEGER,
      maxSize: Number.MAX_SAFE_INTEGER
    },
    enableExpanding: isExpandable
  });
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(RegularTable_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TableContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Thead, {
        children: table.getHeaderGroups().map(function (headerGroup) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRow, {
            children: headerGroup.headers.map(function (header) {
              return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableHeadItem, {
                colSpan: header.colSpan,
                children: header.isPlaceholder ? null : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableHeaderText, {
                    children: Object(build_lib_index_esm["a" /* flexRender */])(header.column.columnDef.header, header.getContext())
                  })
                })
              }, header.id);
            })
          }, headerGroup.id);
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Tbody, {
        children: table.getRowModel().rows.map(function (row, index) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRow, {
            children: row.getVisibleCells().map(function (cell) {
              return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRowItem, {
                row: index,
                children: Object(build_lib_index_esm["a" /* flexRender */])(cell.column.columnDef.cell, cell.getContext())
              }, cell.id);
            })
          }, row.id);
        })
      })]
    })
  });
}
/* harmony default export */ var components_RegularTable = (RegularTable);
// CONCATENATED MODULE: ./src/images/delete-icon-gray.svg
/* harmony default export */ var delete_icon_gray = ("js/dist/5897937ebe94cc6e33a4e9596d08f998.svg");
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/useTable.tsx






var ReferenceTables_useTable_useTable = function useTable(deleteRow, toggleModalUpdateVersion, showVersion) {
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();

  // const { } = useContext(TaskContext);

  var columns = Object(react["useMemo"])(function () {
    return [columnHelper.accessor('reference_table_name', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Table name"
        });
      },
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          title: "Interface: ".concat(row.original.interface_name, ", schema: ").concat(row.original.schema_name, "."),
          children: row.getValue('reference_table_name')
        });
      }
    })].concat(toConsumableArray_default()(showVersion ? [columnHelper.accessor('version_task_name', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Table version"
        });
      },
      cell: function cell(_ref2) {
        var row = _ref2.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          style: {
            cursor: 'pointer',
            color: '#1683f2'
          },
          onClick: function onClick() {
            return toggleModalUpdateVersion(row.original);
          },
          children: row.getValue('version_task_name') || 'None'
        });
      }
    })] : []), [{
      id: 'actions',
      header: '',
      cell: function cell(_ref3) {
        var row = _ref3.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
          onClick: function onClick() {
            return deleteRow(row.original);
          },
          src: delete_icon_gray
        });
      }
    }]);
  }, [columnHelper, deleteRow, showVersion, toggleModalUpdateVersion]);
  return {
    columns: columns
  };
};
/* harmony default export */ var ReferenceTables_useTable = (ReferenceTables_useTable_useTable);
// CONCATENATED MODULE: ./src/components/CustomerTypeTable/styles.ts

var CustomerTypeTable_styles_templateObject, CustomerTypeTable_styles_templateObject2, CustomerTypeTable_styles_templateObject3, CustomerTypeTable_styles_templateObject4, CustomerTypeTable_styles_templateObject5, CustomerTypeTable_styles_templateObject6, CustomerTypeTable_styles_templateObject7, CustomerTypeTable_styles_templateObject8, CustomerTypeTable_styles_templateObject9;

var UpdateTableVersionContainerstyled = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject || (CustomerTypeTable_styles_templateObject = taggedTemplateLiteral_default()(["\n  box-sizing: border-box ;\n    position: fixed;\n    width: 100%;\n    height: 100%;\n    top: 0;\n    left: 0;\n    right: 0;\n    bottom: 0;\n    z-index: 100;\n    display: flex;\n    justify-content: center;\n    align-items: center;\n    background-color: rgba(0, 0, 0, 0.2);\n"])));
var styles_animation = Object(styled_components_browser_esm["c" /* keyframes */])(CustomerTypeTable_styles_templateObject2 || (CustomerTypeTable_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  from {\n    transform: scale(0.9);\n  }\n\n  to {\n    transform: scale(1);\n  }\n"])));
var CustomerTypeTable_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject3 || (CustomerTypeTable_styles_templateObject3 = taggedTemplateLiteral_default()(["\nbox-sizing: border-box ;\n    background: white;\n    box-sizing: border-box;\n    border-radius: 4px;\n    box-shadow: 0 0 6px 0 rgba(0, 0, 0, 0.16);\n    display: flex;\n    flex-direction: column;\n    position: relative;\n    animation: ", " 0.2s linear;\n    width: 85%;\n    background-color: #ffffff;\n    flex-direction: column;\n    justify-content: space-between;\n    align-items: center;\n"])), styles_animation);
var HeaderWrapper = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject4 || (CustomerTypeTable_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    box-sizing: border-box;\n    background-color: #1483f3;\n    width: 100%;\n    height: 40px;\n    display: flex;\n    justify-content: space-between;\n    align-self: start;\n    align-items: center ;\n\n    color: white;\n    gap: 10px;\n    font-size: 18px ;\n    padding: 0 10px;\n"])));
var WrapperTop = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject5 || (CustomerTypeTable_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    box-sizing: border-box;\n    width: 100%;\n    height: 100px;\n    display: flex;\n    justify-content: start;\n    align-items: center;\n    gap: 10px;\n    padding: 20px;\n"])));
var WrapperBottom = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject6 || (CustomerTypeTable_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    box-sizing: border-box;\n    width: 100%;\n    height: 300px;\n    display: flex;\n    justify-content: start;\n    align-items: center;\n    padding: 20px;\n    gap: 10px;\n"])));
var WrapperFooter = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject7 || (CustomerTypeTable_styles_templateObject7 = taggedTemplateLiteral_default()(["\nbox-sizing: border-box ;\n    width: 100%;\n    padding: 0 20px;\n    display: flex;\n    align-items: center ;\n    justify-content: flex-end;\n"])));
var styles_Actions = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject8 || (CustomerTypeTable_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    display: flex;\n    margin-top:5px;\n    align-items: center;\n    justify-content: flex-end;\n    gap: 18px;\n    border-bottom: ", ";\n    padding-bottom: 13px;\n"])), function (props) {
  return props.border ? '1px solid #ccc' : '';
});
var styles_ActionItem = styled_components_browser_esm["b" /* default */].div(CustomerTypeTable_styles_templateObject9 || (CustomerTypeTable_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    cursor: pointer;\n"])));
// CONCATENATED MODULE: ./src/components/CustomerTypeTable/index.tsx






var CustomerTypeTable_MyIcon = function MyIcon(_ref) {
  var _ref$color = _ref.color,
    color = _ref$color === void 0 ? 'white' : _ref$color;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("svg", {
    xmlns: "http://www.w3.org/2000/svg",
    width: "14",
    height: "14",
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])("path", {
      fillRule: "evenodd",
      fill: color,
      width: '10px',
      height: '10px',
      d: "m8.45 6.8 4.53-4.948a1.007 1.007 0 0 0 .003-1.415C12.594.46 11.964.43 11.574.434L6.642 5.381 1.727.414C1.339.23.708.21.32.412c.37.39.37 1.024-.003 1.416l4.914 4.966-4.932 4.947c.601.39.621 1.024-.003 1.416.194.197.449.295.804.295.154 0 .408-.097.602-.292l4.934-4.947 4.914 4.966a.987.987 0 0 0 1.408.003c.388-.39.39-1.024.002-1.416L8.45 6.8z"
    })
  });
};
var CustomerTypeTable_CustomerTypeTable = function CustomerTypeTable(_ref2) {
  var onClose = _ref2.onClose,
    data = _ref2.data,
    column = _ref2.column,
    onClickSave = _ref2.onClickSave,
    tableName = _ref2.tableName;
  var onSave = function onSave() {
    onClickSave();
    onClose();
  };
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(UpdateTableVersionContainerstyled, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(CustomerTypeTable_styles_Wrapper, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(HeaderWrapper, {
        children: [tableName, " data snapshots", /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          onClick: onClose,
          style: {
            cursor: 'pointer'
          },
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(CustomerTypeTable_MyIcon, {})
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(WrapperTop, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
          title: '',
          startDate: new Date(),
          startDateChange: function startDateChange() {},
          endDate: new Date(),
          endDateChange: function endDateChange() {}
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(WrapperBottom, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Table, {
          columns: column,
          data: data
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(WrapperFooter, {
        children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Actions, {
          border: false,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_ActionItem, {
            onClick: function onClick() {
              return onClose();
            },
            children: "Cancel"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_ActionItem, {
            onClick: onSave,
            children: "Save"
          })]
        })
      })]
    })
  });
};
/* harmony default export */ var components_CustomerTypeTable = (CustomerTypeTable_CustomerTypeTable);
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/useCustomerTypeTable.tsx



function useCustomerTypeTable_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function useCustomerTypeTable_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? useCustomerTypeTable_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : useCustomerTypeTable_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }

// {

//     "task_name": "aaaa",

//     "task_description": "",

//     "executed_by": "[tahata@k2view.com##[k2view_k2v_user]](mailto:tahata@k2view.com )",

//     "execution_datetime": "2024-02-13 07:46:01.232883",

//     "task_execution_id": 1,

//     "number_of_records": 10

//   }







var useCustomerTypeTable_useCustomerTable = function useCustomerTable() {
  var _useContext = Object(react["useContext"])(TaskContext),
    _useContext$taskData = _useContext.taskData,
    source_environment_name = _useContext$taskData.source_environment_name,
    tableList = _useContext$taskData.tableList,
    saveForm = _useContext.saveForm;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    data = _useState2[0],
    setData = _useState2[1];
  var _useState3 = Object(react["useState"])(null),
    _useState4 = slicedToArray_default()(_useState3, 2),
    selected = _useState4[0],
    setSelected = _useState4[1];
  var _useState5 = Object(react["useState"])(null),
    _useState6 = slicedToArray_default()(_useState5, 2),
    current = _useState6[0],
    setCurrent = _useState6[1];
  var selectRow = function selectRow(row) {
    setSelected({
      task_execution_id: row.task_execution_id,
      task_name: row.task_name
    });
  };
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  var _useState7 = Object(react["useState"])(false),
    _useState8 = slicedToArray_default()(_useState7, 2),
    showModal = _useState8[0],
    setShowModal = _useState8[1];
  var columns = Object(react["useMemo"])(function () {
    return [{
      id: 'select',
      header: '',
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: function onChange() {
              return selectRow(row.original);
            },
            name: "select",
            value: row.original.task_execution_id,
            selectedValue: selected === null || selected === void 0 ? void 0 : selected.task_execution_id,
            title: ''
          })
        });
      }
    }, columnHelper.accessor('task_name', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "task name"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('task_execution_id', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "task execution id"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('execution_datetime', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Creation date"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('executed_by', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "executed by"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('task_description', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "task description"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    })];
  }, [columnHelper, selected]);
  var OpenModalUpdateVersion = Object(react["useCallback"])( /*#__PURE__*/function () {
    var _ref2 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee(row) {
      var request, _data;
      return regenerator_default.a.wrap(function _callee$(_context) {
        while (1) switch (_context.prev = _context.next) {
          case 0:
            if (!source_environment_name) {
              _context.next = 9;
              break;
            }
            request = {
              table_name: row.reference_table_name,
              env_name: source_environment_name
            };
            _context.next = 4;
            return apis_task.getTableVersions(row.reference_table_name, request.env_name);
          case 4:
            _data = _context.sent;
            setData(_data);
            setShowModal(true);
            setCurrent(row);
            setSelected({
              task_execution_id: row.version_task_execution_id,
              task_name: row.version_task_name
            });
          case 9:
          case "end":
            return _context.stop();
        }
      }, _callee);
    }));
    return function (_x) {
      return _ref2.apply(this, arguments);
    };
  }(), [source_environment_name]);
  var onClose = function onClose() {
    setShowModal(false);
  };
  var onClickSave = function onClickSave() {
    var updatedData = (tableList || []).map(function (table) {
      return table.reference_table_name === current.reference_table_name ? useCustomerTypeTable_objectSpread(useCustomerTypeTable_objectSpread({}, table), {}, {
        version_task_execution_id: selected === null || selected === void 0 ? void 0 : selected.task_execution_id,
        version_task_name: selected === null || selected === void 0 ? void 0 : selected.task_name
      }) : table;
    });
    saveForm({
      tableList: updatedData
    });
  };
  return {
    columns: columns,
    OpenModalUpdateVersion: OpenModalUpdateVersion,
    showModal: showModal,
    onClose: onClose,
    data: data,
    onClickSave: onClickSave,
    current: current
  };
};
/* harmony default export */ var useCustomerTypeTable = (useCustomerTypeTable_useCustomerTable);
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/index.tsx




function ReferenceTables_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function ReferenceTables_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ReferenceTables_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ReferenceTables_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }

















function ReferenceTables(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    register = _useContext.register,
    errors = _useContext.errors;
  console.log('taskData', taskData);
  var source_type = taskData.source_type,
    be_name = taskData.be_name,
    source_environment_name = taskData.source_environment_name,
    fetchPolicy = taskData.fetchPolicy,
    tableList = taskData.tableList,
    sync_mode = taskData.sync_mode,
    selected_logical_units_names = taskData.selected_logical_units_names;
  var showVersion = Object(react["useMemo"])(function () {
    return fetchPolicy === 'load_snapshot' || fetchPolicy === 'available_data';
  }, [fetchPolicy]);
  console.log('showVersion', showVersion);
  var _useState = Object(react["useState"])(false),
    _useState2 = slicedToArray_default()(_useState, 2),
    loading = _useState2[0],
    setLoading = _useState2[1];
  var _useState3 = Object(react["useState"])(''),
    _useState4 = slicedToArray_default()(_useState3, 2),
    filter = _useState4[0],
    setFilter = _useState4[1];
  Object(react["useEffect"])(function () {
    function fetchData() {
      return _fetchData.apply(this, arguments);
    }
    function _fetchData() {
      _fetchData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data, transformedEnvs, newTableList;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              if (source_environment_name) {
                _context.next = 3;
                break;
              }
              return _context.abrupt("return");
            case 3:
              _context.next = 5;
              return apis_task.getTableByBeAndEnv(source_environment_name || '', be_name);
            case 5:
              data = _context.sent;
              transformedEnvs = data.map(function (item, index) {
                // Extract database name and tables

                var dbName = Object.keys(item)[0]; // e.g., 'CRM_DB'

                var schemas = {};
                item[dbName].forEach(function (schema) {
                  var schemaKey = Object.keys(schema)[0];
                  schemas[schemaKey] = {
                    tables: [],
                    opened: false
                  };
                  var tables = schema[schemaKey].filter(function (it) {
                    return (sync_mode === 'OFF' && it.taskExecutionId || sync_mode !== 'OFF') && ((selected_logical_units_names || []).indexOf(it.luName) >= 0 || !be_name);
                  }).map(function (table) {
                    var old = tableList !== null && tableList !== void 0 ? tableList : [];
                    var isFound = old.find(function (el) {
                      return el.reference_table_name === table.tableName;
                    });
                    var isMoved = !!isFound;
                    return {
                      name: table.tableName,
                      version_task_execution_id: table.taskExecutionId,
                      version_task_name: table.taskName,
                      schema_name: schemaKey,
                      lu_name: table.luName,
                      selected: false,
                      moved: isMoved
                    };
                  });
                  schemas[schemaKey].tables = tables;
                  schemas[schemaKey].opened = true;
                });

                // Construct and return the Env object
                return {
                  env_name: dbName,
                  opened: true,
                  // Set default values
                  openedTables: true,
                  // Set default values
                  schemas: schemas
                };
              });
              newTableList = tableList === null || tableList === void 0 ? void 0 : tableList.filter(function (item_selected) {
                var interfaceData = transformedEnvs.find(function (item) {
                  return item.env_name === item_selected.interface_name;
                });
                if (interfaceData && interfaceData.schemas[item_selected.schema_name]) {
                  var schemaData = interfaceData.schemas[item_selected.schema_name];
                  if (schemaData && schemaData.tables.findIndex(function (it) {
                    return it.name === item_selected.reference_table_name;
                  }) >= 0) {
                    return true;
                  }
                }
                return false;
              });
              saveForm({
                tableList: newTableList
              });
              setTablesData(transformedEnvs);
              _context.next = 15;
              break;
            case 12:
              _context.prev = 12;
              _context.t0 = _context["catch"](0);
              // use hook toast
              console.log(_context.t0);
            case 15:
              _context.prev = 15;
              setLoading(false);
              return _context.finish(15);
            case 18:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 12, 15, 18]]);
      }));
      return _fetchData.apply(this, arguments);
    }
    fetchData();
  }, [be_name, source_environment_name, source_type, sync_mode, selected_logical_units_names]);
  Object(react["useEffect"])(function () {
    var old = tableList !== null && tableList !== void 0 ? tableList : [];
    setTablesData(function (prevEnvsState) {
      return prevEnvsState.map(function (env) {
        var updatedSchemas = Object.keys(env.schemas).reduce(function (acc, schemaKey) {
          if (!acc[schemaKey]) {
            acc[schemaKey] = {
              tables: [],
              opened: env.schemas[schemaKey].opened
            };
          }
          var updatedTables = env.schemas[schemaKey].tables.map(function (table) {
            var isFound = old.find(function (el) {
              return el.reference_table_name === table.name && el.schema_name === table.schema_name && el.interface_name === env.env_name;
            });
            var isMoved = !!isFound;
            return ReferenceTables_objectSpread(ReferenceTables_objectSpread({}, table), {}, {
              moved: isMoved,
              selected: false
            });
          });
          acc[schemaKey].tables = updatedTables;
          return acc;
        }, {});
        console.log('updatedSchemas', updatedSchemas);
        return ReferenceTables_objectSpread(ReferenceTables_objectSpread({}, env), {}, {
          schemas: updatedSchemas
        });
      });
    });
  }, [source_environment_name, tableList]);
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    tablesData = _useState6[0],
    setTablesData = _useState6[1];
  var toggleBE = Object(react["useCallback"])(function (BE) {
    setTablesData(function (prevData) {
      var foundBE = prevData.find(function (it) {
        return it.env_name === BE;
      });
      if (foundBE) {
        foundBE.opened = !foundBE.opened;
      }
      return toConsumableArray_default()(prevData);
    });
  }, [setTablesData]);
  var toggleTables = Object(react["useCallback"])(function (BE, schemaKey) {
    setTablesData(function (prevData) {
      var foundBE = prevData.find(function (it) {
        return it.env_name === BE;
      });
      if (foundBE) {
        foundBE.schemas[schemaKey].opened = !foundBE.schemas[schemaKey].opened;
      }
      return toConsumableArray_default()(prevData);
    });
  }, [setTablesData]);
  var toggleTable = Object(react["useCallback"])(function (BE, table_name, schemaKey) {
    setTablesData(function (prevData) {
      var foundBE = prevData.find(function (it) {
        return it.env_name === BE;
      });
      if (foundBE) {
        var table = foundBE.schemas[schemaKey].tables.find(function (it) {
          return it.name === table_name;
        });
        if (table) {
          table.selected = !table.selected;
        }
      }
      return toConsumableArray_default()(prevData);
    });
  }, [setTablesData]);
  var getTable = Object(react["useCallback"])(function (BE, tableData, schemaKey) {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableItemContainer, {
      selected: tableData.selected,
      onClick: function onClick() {
        return toggleTable(BE, tableData.name, schemaKey);
      },
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
        width: '17px',
        src: table_icon
      }), tableData.name]
    });
  }, [toggleTable]);
  var getEnvTables = Object(react["useCallback"])(function (env, schem, schemaKey) {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: env.opened ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TablesIconContainer, {
          onClick: function onClick() {
            return toggleTables(env.env_name, schemaKey);
          },
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
            width: '21px',
            src: tables_folder_icon
          }), schemaKey]
        }), schem.opened ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables, {
          children: schem.tables.filter(function (it) {
            return it.name.toLowerCase().indexOf((filter || '').toLowerCase()) >= 0 || !filter;
          }).filter(function (it) {
            return !it.moved;
          }).map(function (it) {
            return getTable(env.env_name, it, schemaKey);
          })
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
    });
  }, [filter, getTable, toggleBE, toggleTables]);
  var movedTables = Object(react["useMemo"])(function () {
    return tableList !== null && tableList !== void 0 ? tableList : [];
  }, [tableList]);
  var moveTables = function moveTables() {
    var old = tableList !== null && tableList !== void 0 ? tableList : [];
    var updatedTables = [];
    tablesData.forEach(function (env) {
      // Iterate over each schema within the environment
      Object.entries(env.schemas).forEach(function (_ref) {
        var _ref2 = slicedToArray_default()(_ref, 2),
          schemaKey = _ref2[0],
          schema = _ref2[1];
        // Filter for selected tables within this schema
        var movedTables = schema.tables.filter(function (table) {
          return table.selected;
        });
        movedTables.forEach(function (table) {
          updatedTables.push({
            reference_table_name: table.name,
            interface_name: env.env_name,
            // Assuming this is meant to identify the environment/schema
            version_task_execution_id: table.version_task_execution_id,
            version_task_name: table.version_task_name,
            schema_name: table.schema_name,
            lu_name: table.lu_name
          });
        });
      });
    });
    saveForm({
      tableList: [].concat(toConsumableArray_default()(old), updatedTables)
    });
  };
  var deleteRow = Object(react["useCallback"])(function (row) {
    var updatedTables = tableList || [];
    updatedTables = updatedTables.filter(function (table) {
      return !(table.reference_table_name === row.reference_table_name && table.schema_name === row.schema_name && table.interface_name === row.interface_name);
    });
    saveForm({
      tableList: updatedTables
    });
  }, [tableList, saveForm]);
  var _useCustomerTable = useCustomerTypeTable(),
    customerTableColumns = _useCustomerTable.columns,
    customerTableData = _useCustomerTable.data,
    OpenModalUpdateVersion = _useCustomerTable.OpenModalUpdateVersion,
    showModal = _useCustomerTable.showModal,
    onClose = _useCustomerTable.onClose,
    onClickSave = _useCustomerTable.onClickSave,
    current = _useCustomerTable.current;
  var _useTable = ReferenceTables_useTable(deleteRow, OpenModalUpdateVersion, showVersion),
    columns = _useTable.columns;
  Object(react["useEffect"])(function () {
    console.log('tablesData', tablesData);
  }, [tablesData]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ReferenceTables_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Title, {
      children: "Tables"
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TablesContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(SourceTablesContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
          title: '',
          placeholder: "Type to filter...",
          width: '100%',
          value: filter,
          onChange: setFilter,
          type: InputTypes.text
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SourceTables, {
          children: tablesData.map(function (env) {
            return /*#__PURE__*/Object(jsx_runtime["jsxs"])(react_default.a.Fragment, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(EnvIconContainer, {
                onClick: function onClick() {
                  return toggleBE(env.env_name);
                },
                children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
                  width: '19px',
                  src: env_icon
                }), env.env_name]
              }), Object.entries(env.schemas).map(function (_ref3) {
                var _ref4 = slicedToArray_default()(_ref3, 2),
                  schemaKey = _ref4[0],
                  schem = _ref4[1];
                return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
                  children: getEnvTables(env, schem, schemaKey)
                }, schemaKey);
              })]
            }, env.env_name);
          })
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(MoveTablesButton, {
        onClick: moveTables,
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
          src: arrow_right
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectedTables, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RegularTable, {
          columns: columns,
          data: movedTables
        })
      })]
    }), showModal && /*#__PURE__*/Object(jsx_runtime["jsx"])(components_CustomerTypeTable, {
      tableName: current === null || current === void 0 ? void 0 : current.reference_table_name,
      data: customerTableData,
      column: customerTableColumns,
      onClose: onClose,
      onClickSave: onClickSave
    })]
  });
}
/* harmony default export */ var task_ReferenceTables = (ReferenceTables);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSourceSettings/index.tsx



function DataSourceSettings_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function DataSourceSettings_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? DataSourceSettings_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : DataSourceSettings_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }















function DataSourceSettingsForm(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    unregister = _useContext.unregister;
  var be_name = taskData.be_name,
    dataSourceType = taskData.dataSourceType,
    source_environment_id = taskData.source_environment_id,
    mask_sensitive_data = taskData.mask_sensitive_data,
    sync_mode = taskData.sync_mode,
    synthetic_type = taskData.synthetic_type,
    source_environment_name = taskData.source_environment_name,
    environment_sync_mode = taskData.environment_sync_mode,
    version_ind = taskData.version_ind,
    be_id = taskData.be_id,
    source_type = taskData.source_type,
    tables_selected = taskData.tables_selected,
    sourceUserRole = taskData.sourceUserRole,
    fetchPolicy = taskData.fetchPolicy,
    generateChosenParams = taskData.generateChosenParams,
    dataGenerationParams = taskData.dataGenerationParams,
    enable_masking_only = taskData.enable_masking_only;
  var _useState = Object(react["useState"])(mask_sensitive_data || false),
    _useState2 = slicedToArray_default()(_useState, 2),
    maskSensitiveDataLocal = _useState2[0],
    setMaskSensitiveDataLocal = _useState2[1];
  Object(react["useEffect"])(function () {
    var updateData = {};
    if (!dataSourceType) {
      updateData.dataSourceType = 'data_source';
      updateData.source_type = 'BE';
    }
    if (!synthetic_type) {
      updateData.synthetic_type = 'new_data';
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
  }, []);
  var fetchDataPolicyItems = Object(react["useMemo"])(function () {
    var dataMapper = {
      new_data: {
        value: 'new_data',
        label: "Available data from the Test data store, new data from ".concat(source_environment_name)
      },
      all_data: {
        value: 'all_data',
        label: "All data from ".concat(source_environment_name)
      },
      load_snapshot: {
        value: 'load_snapshot',
        label: "Selected snapshot (version)"
      },
      available_data: {
        value: 'available_data',
        label: "Available ".concat(source_environment_name, " data in the Test data store")
      }
    };
    var result = [];
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      if (environment_sync_mode === 'OFF') {
        result = [dataMapper.load_snapshot];
      } else {
        result = [dataMapper.all_data, dataMapper.load_snapshot];
      }
    } else if (environment_sync_mode === 'OFF') {
      result = [dataMapper.available_data, dataMapper.load_snapshot];
    } else {
      result = [dataMapper.new_data, dataMapper.all_data, dataMapper.available_data, dataMapper.load_snapshot];
    }
    if (sourceUserRole && !sourceUserRole.allowed_request_of_fresh_data) {
      result = result.filter(function (it) {
        return it.value !== 'all_data';
      });
    }
    return result;
  }, [environment_sync_mode, source_environment_name, dataSourceType, source_type, sourceUserRole]);
  var _useState3 = Object(react["useState"])(fetchDataPolicyItems[0]),
    _useState4 = slicedToArray_default()(_useState3, 2),
    fecthDataPolicyLocal = _useState4[0],
    setFecthDataPolicyLocal = _useState4[1];
  Object(react["useEffect"])(function () {
    setMaskSensitiveDataLocal(mask_sensitive_data || false);
  }, [mask_sensitive_data]);
  Object(react["useEffect"])(function () {
    if (dataSourceType === 'synthetic') {
      // saveForm({
      //     selection_method: 'GENERATE',
      // });
    }
  }, [dataSourceType]);

  // useEffect(() => {
  //     if (dataSourceType === 'synthetic' || 
  //         dataSourceType === 'ai_generated' || 
  //         (dataSourceType === 'data_source' &&  source_environment_id && (fecthDataPolicyLocal?.value === 'available_data' || 
  //         fecthDataPolicyLocal?.value === 'load_snapshot'))) {
  //             saveForm({
  //                 load_entity: true,
  //             });
  //     } 
  //     // else {
  //     //     saveForm({
  //     //         load_entity: false,
  //     //     });
  //     // }
  // }, [dataSourceType, fecthDataPolicyLocal]);

  Object(react["useEffect"])(function () {
    var getDataByValue = function getDataByValue(value) {
      return fetchDataPolicyItems.find(function (it) {
        return it.value === value;
      });
    };
    if (sync_mode === 'ON') {
      setFecthDataPolicyLocal(getDataByValue('new_data'));
    } else if (sync_mode === 'FORCE') {
      setFecthDataPolicyLocal(getDataByValue('all_data'));
    } else {
      if (version_ind) {
        setFecthDataPolicyLocal(getDataByValue('load_snapshot'));
      } else {
        setFecthDataPolicyLocal(getDataByValue('available_data'));
      }
    }
  }, [sync_mode, version_ind]);
  Object(react["useEffect"])(function () {
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      saveForm({
        tables_selected: true
      });
    }
  }, [dataSourceType, source_type]);
  var dataSourceTypeChange = Object(react["useCallback"])(function (dataSourceTypeNew) {
    var source_type = '';
    var dataSourceType = dataSourceTypeNew;
    var updateBE = {
      selection_method: 'L',
      tableList: [],
      tables_selected: false,
      source_environment_id: undefined,
      source_environment_name: '',
      selected_logical_units_names: [],
      selected_logical_units: []
    };
    if (dataSourceTypeNew === 'data_source_BE') {
      dataSourceType = 'data_source';
      source_type = 'BE';
    } else if (dataSourceTypeNew === 'data_source_tables') {
      dataSourceType = 'data_source';
      source_type = 'tables';
      updateBE.be_id = -1;
      updateBE.be_name = undefined;
      updateBE.selection_method = 'TABLES';
      updateBE.tables_selected = false;
      unregister('be_name');
    }
    saveForm(DataSourceSettings_objectSpread({
      source_environment_id: undefined,
      source_environment_name: '',
      sync_mode: 'ON',
      version_ind: false,
      dataSourceType: dataSourceType,
      source_type: source_type
    }, updateBE));
  }, [saveForm]);
  var envChangeLocal = Object(react["useCallback"])(function (item) {
    var sync_mode = item && item.environment_sync_mode || 'ON';
    var syncModeData = {};
    if (sync_mode === 'OFF') {
      syncModeData.sync_mode = 'OFF';
      syncModeData.version_ind = false;
    } else {
      syncModeData.sync_mode = 'ON';
      syncModeData.version_ind = false;
    }
    if (dataSourceType === 'data_source' && source_type !== 'tables') {
      syncModeData.tables_selected = false;
      syncModeData.tableList = [];
    } else if (dataSourceType === 'data_source' && source_type === 'tables') {
      syncModeData.version_ind = true;
    }
    saveForm(DataSourceSettings_objectSpread({
      source_environment_id: item && item.environment_id || undefined,
      source_environment_name: item && item.environment_name || undefined,
      synthetic_indicator: item && item.synthetic_indicator || false,
      mask_sensitive_data: item && item.mask_sensitive_data || false,
      environment_sync_mode: item && item.environment_sync_mode || 'ON'
    }, syncModeData));
  }, [saveForm, dataSourceType, source_type]);
  Object(react["useEffect"])(function () {
    if (environment_sync_mode === 'OFF') {
      if (['new_data', 'all_data'].indexOf(fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value) >= 0) {
        saveForm({
          sync_mode: 'OFF',
          version_ind: false
        });
      }
    }
  }, [environment_sync_mode]);
  var updateGenerationParamValues = Object(react["useCallback"])(function (values) {
    if (values && values.length > 0) {
      var copyGenerationParams = JSON.parse(JSON.stringify(dataGenerationParams));
      values.forEach(function (data) {
        if (copyGenerationParams[data.name]) {
          copyGenerationParams[data.name].value = data.value;
          if (copyGenerationParams[data.name].editor) {
            copyGenerationParams[data.name].editor.value = data.value;
          }
        }
      });
      saveForm({
        dataGenerationParams: copyGenerationParams
      });
    }
  }, [saveForm, dataGenerationParams]);
  var updateChosenParams = Object(react["useCallback"])(function (data) {
    var updateData = {};
    var copyDataGenerationParams = JSON.parse(JSON.stringify(dataGenerationParams));
    var copyGenerateChosenParams = toConsumableArray_default()(generateChosenParams);
    if (data.action === 'add') {
      if (copyGenerateChosenParams.length === 0) {
        copyDataGenerationParams[data.key].order = 1;
      } else {
        var key = copyGenerateChosenParams[copyGenerateChosenParams.length - 1];
        copyDataGenerationParams[data.key].order = copyDataGenerationParams[key].order + 1;
      }
      updateData.dataGenerationParams = copyDataGenerationParams;
      updateData.generateChosenParams = [].concat(toConsumableArray_default()(copyGenerateChosenParams), [data.key]);
    } else {
      copyDataGenerationParams[data.key].editor.value = copyDataGenerationParams[data.key].default;
      copyDataGenerationParams[data.key].value = copyDataGenerationParams[data.key].default;
      copyDataGenerationParams[data.key].order = 99999999;
      updateData.dataGenerationParams = copyDataGenerationParams;
      updateData.generateChosenParams = copyGenerateChosenParams.filter(function (key) {
        return key !== data.key;
      });
    }
    saveForm(updateData);
  }, [dataGenerationParams, saveForm, generateChosenParams]);
  var fetchDataPolicyChange = Object(react["useCallback"])(function (item) {
    var updateData = {};
    if ((item === null || item === void 0 ? void 0 : item.value) === 'new_data') {
      updateData.sync_mode = 'ON';
      updateData.version_ind = false;
    } else if ((item === null || item === void 0 ? void 0 : item.value) === 'all_data') {
      updateData.sync_mode = 'FORCE';
      if (dataSourceType === 'data_source' && source_type === 'tables') {
        updateData.version_ind = true;
      } else {
        updateData.version_ind = false;
      }
    } else if ((item === null || item === void 0 ? void 0 : item.value) === 'available_data') {
      updateData.sync_mode = 'OFF';
      updateData.version_ind = false;
    } else if ((item === null || item === void 0 ? void 0 : item.value) === 'load_snapshot') {
      updateData.sync_mode = 'OFF';
      updateData.version_ind = true;
      updateData.tableList = [];
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
  }, [saveForm, dataSourceType, source_type]);
  Object(react["useEffect"])(function () {
    if ((fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value) !== fetchPolicy) {
      saveForm({
        fetchPolicy: fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value
      });
    }
  }, [fecthDataPolicyLocal]);
  var syntheticTypeMapper = {
    data_source: 'None',
    synthetic: 'RuleBased',
    ai_generated: 'AI'
  };
  var getDataPolicy = Object(react["useCallback"])(function () {
    var value = fetchDataPolicyItems.find(function (it) {
      return it.value === (fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value);
    });
    if (!value) {
      value = fetchDataPolicyItems[0];
    }
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
      width: "auto",
      title: 'Policy for fetching data'
      // mandatory={!selectedBe}
      ,
      isMulti: false,
      value: value,
      options: fetchDataPolicyItems,
      onChange: fetchDataPolicyChange
    });
  }, [fecthDataPolicyLocal, fetchDataPolicyItems, fetchDataPolicyChange]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Wrapper, {
    children: [!enable_masking_only ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSourceTypes, {
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(SyntheticContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: dataSourceTypeChange,
          name: "data_source_type",
          value: "data_source_BE",
          selectedValue: "".concat(dataSourceType, "_").concat(source_type),
          title: "Entities & referential data"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: dataSourceTypeChange,
          name: "data_source_type",
          value: "data_source_tables",
          selectedValue: "".concat(dataSourceType, "_").concat(source_type),
          title: "Tables"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: dataSourceTypeChange,
          name: "data_source_type",
          value: "synthetic",
          selectedValue: dataSourceType,
          title: "Rule based generation"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: dataSourceTypeChange,
          name: "data_source_type",
          value: "ai_generated",
          selectedValue: dataSourceType,
          title: "AI based generation"
        })]
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(DataSourceContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(EnvironmentsContainer, {
        data_source: dataSourceType === 'data_source',
        children: [source_type === 'BE' && dataSourceType === 'data_source' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DataMovmentSettingsContainer, {
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_DataMovmentSettings, {
            enabledTabs: ['be'],
            type: 'source'
          })
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), be_id || dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
          children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(EnvironmentAndMaskData, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_EnvironmentSelect, {
              mode: "SOURCE",
              title: "Source environment",
              syntheticType: syntheticTypeMapper[dataSourceType],
              be_name: be_name,
              environment_id: source_environment_id,
              onChange: envChangeLocal,
              isMandatory: false
            }), source_environment_id ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(MaskDataContainer, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Icon, {
                src: pii_icon
              }), maskSensitiveDataLocal ? 'Sensitive data is masked' : 'Data is not masked']
            }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
          }), source_environment_id ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(FetchDataPolicyContainer, {
            children: [getDataPolicy(), source_type !== 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                name: 'reference_tables',
                disabled: !(sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_refresh_reference_data),
                title: 'Referential tables',
                onChange: function onChange(value) {
                  saveForm({
                    tables_selected: value
                  });
                },
                value: tables_selected
              })
            }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      }), tables_selected && source_environment_id ? /*#__PURE__*/Object(jsx_runtime["jsx"])(task_ReferenceTables, {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), dataSourceType === 'synthetic' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_DataGenerationParameters, {
      dataGenerationParams: dataGenerationParams,
      updateParams: updateChosenParams,
      chosenParams: generateChosenParams,
      updateValues: updateGenerationParamValues
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), dataSourceType === 'ai_generated' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_SelectTrainingModels, {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var DataSourceSettings = (DataSourceSettingsForm);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/styles.ts

var DataSubset_styles_templateObject, DataSubset_styles_templateObject2, DataSubset_styles_templateObject3, DataSubset_styles_templateObject4, DataSubset_styles_templateObject5, DataSubset_styles_templateObject6, DataSubset_styles_templateObject7, DataSubset_styles_templateObject8, DataSubset_styles_templateObject9, DataSubset_styles_templateObject10, DataSubset_styles_templateObject11, DataSubset_styles_templateObject12;

var DataSubset_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject || (DataSubset_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    gap: 30px;\n"])));
var DataSubset_styles_Container = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject2 || (DataSubset_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    max-width: 80vw;\n    position: relative;\n"])));
var DataSubsetsTypes = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject3 || (DataSubset_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n    padding-bottom: 30px;\n    border-bottom: solid 1px #ccc;\n    width: 872px;\n"])));
var styles_EnvironmentsContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject4 || (DataSubset_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 290px;\n"])));
var DataSubset_styles_Icon = styled_components_browser_esm["b" /* default */].img(DataSubset_styles_templateObject5 || (DataSubset_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var DatasetIconContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject6 || (DataSubset_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 8px;\n"])));
var SelectMethodSelectContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject7 || (DataSubset_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    gap: 25px;\n    align-items: flex-start;\n"])));
// export const SelectMethodSelectContainer = styled.div`
//     width: 350px;
// `

var DataVersioningContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject8 || (DataSubset_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n"])));
var DataGenerationContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject9 || (DataSubset_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n"])));
var GenerationTypeOptions = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject10 || (DataSubset_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    align-self: flex-start;\n    display: flex;\n    flex-direction: column;\n    gap: 10px;\n"])));
var DataSubset_styles_Seprator = styled_components_browser_esm["b" /* default */].span(DataSubset_styles_templateObject11 || (DataSubset_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    border-right: 1px solid #ccc;\n    width: 1px;\n    height: ", ";\n"])), function (props) {
  return props.expand ? '105px' : '70px';
});
var NumberOfEntitiesContainer = styled_components_browser_esm["b" /* default */].div(DataSubset_styles_templateObject12 || (DataSubset_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    align-items: flex-start;\n    gap: 20px;\n"])));
// CONCATENATED MODULE: ./src/components/TextArea/styles.ts

var TextArea_styles_templateObject, TextArea_styles_templateObject2, TextArea_styles_templateObject3, TextArea_styles_templateObject4;

var TextArea_styles_Container = styled_components_browser_esm["b" /* default */].div(TextArea_styles_templateObject || (TextArea_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
var TextArea_styles_Title = styled_components_browser_esm["b" /* default */].div(TextArea_styles_templateObject2 || (TextArea_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    margin-bottom: 10px;\n"])));
var TextArea_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span(TextArea_styles_templateObject3 || (TextArea_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    color: red;\n"])));
var TextArea = styled_components_browser_esm["b" /* default */].textarea(TextArea_styles_templateObject4 || (TextArea_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 15px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n    padding: 9px 10px;\n    border-radius: 3px;\n    border: solid 1px #ccc;\n    width: -webkit-fill-available;\n    width: -moz-available;\n    resize: none;\n    :placeholder{\n        font-size: 15px;\n        font-weight: normal;\n        font-stretch: normal;\n        font-style: normal;\n        line-height: 1.33;\n        letter-spacing: normal;\n        text-align: left;\n        color: #999;\n    }\n"])));
// CONCATENATED MODULE: ./src/components/TextArea/index.tsx






function TDMTextArea(props) {
  var title = props.title,
    value = props.value,
    onChange = props.onChange,
    name = props.name,
    mandatory = props.mandatory,
    placeholder = props.placeholder,
    error = props.error;
  var onChangeLocal = Object(react["useCallback"])(function (event) {
    onChange(event.target.value);
  }, [onChange]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TextArea_styles_Container, {
    children: [title ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(TextArea_styles_Title, {
      children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(TextArea_styles_MadatoryAsterisk, {
        children: mandatory ? '*' : ''
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(TextArea, {
      rows: 6,
      placeholder: placeholder,
      name: name,
      value: value,
      onChange: onChangeLocal
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
      error: error
    })]
  });
}
/* harmony default export */ var components_TextArea = (TDMTextArea);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/EntityList.tsx

function EntityList_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function EntityList_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? EntityList_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : EntityList_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }





function EntityList(props) {
  var _errors$selection_par;
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    resetField = _useContext.resetField,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var selection_param_value = taskData.selection_param_value,
    maxToCopy = taskData.maxToCopy;
  var localChange = Object(react["useCallback"])(function (value) {
    saveForm({
      selection_param_value: value,
      num_of_entities: (value || '').split(',').length
    });
  }, [saveForm]);
  var validateEntites = Object(react["useCallback"])(function (value) {
    if (value && value.split(',').length > (maxToCopy || 0)) {
      return "The number of entities cannot exceed ".concat(maxToCopy || 0, " entities.");
    }
    // const pattern = new RegExp(
    //     '^((\\s*\\w\\s*|-)+(?:,(\\s*\\w\\s*|-)+){0,' +
    //         ((maxToCopy || 1000000000) - 1) +
    //         '})?$'
    // );
    // if (!pattern.test(value || '')) {
    //     return 'The entity ID must consist of letters, numbers or a dash only. Other characters are not supported.';
    // }
    return true;
  }, [maxToCopy]);
  Object(react["useEffect"])(function () {
    return function () {
      unregister('selection_param_value');
    };
  }, []);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TextArea, EntityList_objectSpread(EntityList_objectSpread({}, register('selection_param_value', {
      value: selection_param_value,
      required: 'Populate entities',
      validate: {
        validateEntites: validateEntites
      }
    })), {}, {
      name: "selection_param_value",
      title: "Enter entity IDs separated by commas",
      mandatory: true,
      min: 1,
      value: selection_param_value,
      onChange: localChange,
      error: (_errors$selection_par = errors.selection_param_value) === null || _errors$selection_par === void 0 ? void 0 : _errors$selection_par.message
    }))
  });
}
/* harmony default export */ var DataSubset_EntityList = (EntityList);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/CustomLogic/styles.ts

var CustomLogic_styles_templateObject, CustomLogic_styles_templateObject2, CustomLogic_styles_templateObject3, CustomLogic_styles_templateObject4, CustomLogic_styles_templateObject5;

var CustomLogic_styles_Container = styled_components_browser_esm["b" /* default */].div(CustomLogic_styles_templateObject || (CustomLogic_styles_templateObject = taggedTemplateLiteral_default()(["\n    border-top: 1px solid #ccc;\n    padding-top: 30px;\n    width: 100%;\n    display: flex;\n    position: relative;\n"])));
var styles_LeftSide = styled_components_browser_esm["b" /* default */].div(CustomLogic_styles_templateObject2 || (CustomLogic_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    position: relative;\n    display: flex;\n    flex-direction: column;\n    align-items: flex-start;\n    gap: 25px;\n    width: 326px;\n    border-right: 1px solid #ccc;\n"])));
var CustomLogic_styles_Seprator = styled_components_browser_esm["b" /* default */].div(CustomLogic_styles_templateObject3 || (CustomLogic_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    border-right: 1px solid #ccc;\n    width: 1px;\n    position: absolute;\n    height: calc(100% + 80px);\n    top: 0px;\n    left: 400px;\n"])));
var SelectContainer = styled_components_browser_esm["b" /* default */].div(CustomLogic_styles_templateObject4 || (CustomLogic_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 287px;\n"])));
var Params = styled_components_browser_esm["b" /* default */].div(CustomLogic_styles_templateObject5 || (CustomLogic_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    width: calc(100% - 400px);\n    margin-left: 60px;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/CustomLogic/index.tsx




function CustomLogic_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function CustomLogic_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? CustomLogic_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : CustomLogic_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }












function CustomLogic(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    register = _useContext.register;
  var selection_param_value = taskData.selection_param_value,
    parameters = taskData.parameters,
    be_name = taskData.be_name,
    source_environment_name = taskData.source_environment_name,
    environment_name = taskData.environment_name,
    customLogicParams = taskData.customLogicParams;
  var _useState = Object(react["useState"])(true),
    _useState2 = slicedToArray_default()(_useState, 2),
    loading = _useState2[0],
    setLoading = _useState2[1];
  var _useState3 = Object(react["useState"])([]),
    _useState4 = slicedToArray_default()(_useState3, 2),
    customLogicFlows = _useState4[0],
    setCustomLogicFlows = _useState4[1];
  var _useState5 = Object(react["useState"])(null),
    _useState6 = slicedToArray_default()(_useState5, 2),
    selectedCustomLogicFlow = _useState6[0],
    setSelectedCustomLogicFlow = _useState6[1];
  Object(react["useEffect"])(function () {
    function fetchCutomLogicFlows() {
      return _fetchCutomLogicFlows.apply(this, arguments);
    }
    function _fetchCutomLogicFlows() {
      _fetchCutomLogicFlows = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data, found;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              if (!(!be_name || !(environment_name || source_environment_name))) {
                _context.next = 4;
                break;
              }
              setLoading(false);
              return _context.abrupt("return");
            case 4:
              _context.next = 6;
              return apis_task.getCustomLogicFlows(be_name, environment_name || source_environment_name || '');
            case 6:
              data = _context.sent;
              data.forEach(function (flow) {
                flow.value = "".concat(flow.luName, "#").concat(flow.flowName);
                flow.label = flow.flowName;
                flow.description = flow.Description;
              });
              if (selection_param_value) {
                found = data.find(function (it) {
                  return it.flowName === selection_param_value;
                });
                if (found) {
                  setSelectedCustomLogicFlow(found);
                }
              }
              setCustomLogicFlows(data);
              setLoading(false);
              _context.next = 16;
              break;
            case 13:
              _context.prev = 13;
              _context.t0 = _context["catch"](0);
              // use hook toast
              setLoading(false);
            case 16:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 13]]);
      }));
      return _fetchCutomLogicFlows.apply(this, arguments);
    }
    fetchCutomLogicFlows();
  }, []);
  Object(react["useEffect"])(function () {
    function fetchCutomLogicParams() {
      return _fetchCutomLogicParams.apply(this, arguments);
    }
    function _fetchCutomLogicParams() {
      _fetchCutomLogicParams = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
        var data, params;
        return regenerator_default.a.wrap(function _callee2$(_context2) {
          while (1) switch (_context2.prev = _context2.next) {
            case 0:
              _context2.prev = 0;
              if (selectedCustomLogicFlow) {
                _context2.next = 3;
                break;
              }
              return _context2.abrupt("return");
            case 3:
              console.log(selectedCustomLogicFlow);
              _context2.next = 6;
              return apis_task.getCustomLogicParams(selectedCustomLogicFlow.luName, selectedCustomLogicFlow.flowName);
            case 6:
              data = _context2.sent;
              data.forEach(function (param) {
                if (param.editor) {
                  param.name = param.editor.name;
                  if (param.editor && Object.keys(param.editor).length === 0 && Object.getPrototypeOf(param.editor) === Object.prototype) {
                    param.editor = undefined;
                  } else {
                    param.editor.value = param.default;
                  }
                }
                if (param.name) {
                  param.displayName = param.name.replace('_', ' ');
                }
                if (param.editor && param.editor.name) {
                  param.displayName = param.editor.name.replace('_', ' ');
                }
              });
              if (parameters) {
                try {
                  params = JSON.parse(parameters);
                  if (params && params.inputs) {
                    data.forEach(function (customParam) {
                      if (customParam.type === 'bool' && !customParam.default) {
                        customParam.default = false;
                      }
                      var param = params.inputs.find(function (it) {
                        return it.name === customParam.name;
                      });
                      if (param) {
                        customParam.value = param.value || customParam.default;
                        if (customParam.editor) {
                          customParam.editor.value = param.value || customParam.default;
                        }
                      }
                    });
                  }
                } catch (err) {
                  console.log(err);
                }
              }
              saveForm({
                customLogicParams: data
              });
              setLoading(false);
              _context2.next = 16;
              break;
            case 13:
              _context2.prev = 13;
              _context2.t0 = _context2["catch"](0);
              // use hook toast
              setLoading(false);
            case 16:
            case "end":
              return _context2.stop();
          }
        }, _callee2, null, [[0, 13]]);
      }));
      return _fetchCutomLogicParams.apply(this, arguments);
    }
    fetchCutomLogicParams();
  }, [selectedCustomLogicFlow]);

  // useEffect(() => {
  //     if (!selectedCustomLogicFlow) {
  //         saveForm({
  //             customLogicParams: [],
  //         });
  //     }
  // },[selectedCustomLogicFlow])

  var updateFabricEditorValues = function updateFabricEditorValues(values) {
    values.forEach(function (data) {
      updateCustomParamLogicNative(data.name, data.value);
    });
  };
  var updateCustomParamLogicNative = Object(react["useCallback"])(function (name, value) {
    if (!customLogicParams) {
      return;
    }
    var customParamsTemp = toConsumableArray_default()(customLogicParams);
    var index = customParamsTemp.findIndex(function (param) {
      return param.name === name;
    });
    if (index >= 0) {
      customParamsTemp[index].value = value;
      if (customParamsTemp[index] && customParamsTemp[index].editor) {
        var editorTemp = customParamsTemp[index].editor;
        if (editorTemp) {
          editorTemp.value = value;
        }
      }
      var _parameters = {
        inputs: (customParamsTemp || []).map(function (it) {
          return {
            name: it.name,
            type: it.type,
            value: it.value
          };
        })
      };
      saveForm({
        customLogicParams: customLogicParams,
        parameters: JSON.stringify(_parameters)
      });
    }
  }, [customLogicParams]);
  var updateFabricRefInData = Object(react["useCallback"])(function (ref) {
    saveForm({
      widgetRefData: ref
    });
  }, [saveForm]);
  var getFabricParams = Object(react["useCallback"])(function () {
    var _ref;
    if (!selectedCustomLogicFlow) {
      return;
    }
    var fabricWidgetItems = customLogicParams === null || customLogicParams === void 0 ? void 0 : customLogicParams.filter(function (it) {
      return it.editor;
    });
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(fabricWidget, {
        editor: (_ref = fabricWidgetItems || []) === null || _ref === void 0 ? void 0 : _ref.map(function (it) {
          return it.editor;
        }),
        updateValues: updateFabricEditorValues,
        saveRef: updateFabricRefInData
      })
    });
  }, [customLogicParams, updateFabricEditorValues, updateFabricRefInData, selectedCustomLogicFlow]);
  var getParams = Object(react["useCallback"])(function () {
    if (!selectedCustomLogicFlow) {
      return;
    }
    var regularItems = customLogicParams === null || customLogicParams === void 0 ? void 0 : customLogicParams.filter(function (it) {
      return !it.editor;
    });
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: (regularItems || []).map(function (param, index) {
        /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, CustomLogic_objectSpread(CustomLogic_objectSpread({}, register("".concat(param.name, "_").concat(index), {
          value: param.value,
          required: 'Please type entities'
        })), {}, {
          width: "287px",
          title: param.displayName || '',
          mandatory: param.mandatory,
          value: param.value,
          onChange: function onChange(value) {
            return updateCustomParamLogicNative(param.name, value);
          },
          type: param.type === 'integer' || param.type === 'real' ? InputTypes.number : InputTypes.text
        }));
      })
    });
  }, [customLogicParams, updateCustomParamLogicNative, selectedCustomLogicFlow]);
  var updateCustomFlow = Object(react["useCallback"])(function (it) {
    setSelectedCustomLogicFlow(it);
    saveForm({
      selection_param_value: it.flowName,
      custom_logic_lu_name: it.luName,
      parameters: null
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(CustomLogic_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_LeftSide, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
        width: '300px',
        title: "Number of entities"
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        title: "Select custom logic",
        mandatory: true,
        options: customLogicFlows,
        value: selectedCustomLogicFlow,
        onChange: updateCustomFlow,
        width: "300px"
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Params, {
      children: [getFabricParams(), getParams()]
    })]
  });
}
/* harmony default export */ var DataSubset_CustomLogic = (CustomLogic);
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/styles.ts

var SelectDataVerioning_styles_templateObject, SelectDataVerioning_styles_templateObject2, SelectDataVerioning_styles_templateObject3, SelectDataVerioning_styles_templateObject4, SelectDataVerioning_styles_templateObject5, SelectDataVerioning_styles_templateObject6;

var SelectDataVerioning_styles_Container = styled_components_browser_esm["b" /* default */].div(SelectDataVerioning_styles_templateObject || (SelectDataVerioning_styles_templateObject = taggedTemplateLiteral_default()(["\n"])));
var SelectDataVerioning_styles_Title = styled_components_browser_esm["b" /* default */].div(SelectDataVerioning_styles_templateObject2 || (SelectDataVerioning_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n"])));
var styles_DatesContainer = styled_components_browser_esm["b" /* default */].div(SelectDataVerioning_styles_templateObject3 || (SelectDataVerioning_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  margin-top: 20px;\n  margin-bottom: 38px;\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n  display: flex;\n  align-items: center;\n  gap: 30px;\n"])));
var SelectDataVerioning_styles_DateItem = styled_components_browser_esm["b" /* default */].div(SelectDataVerioning_styles_templateObject4 || (SelectDataVerioning_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  display: flex;\n  align-items: center;\n  gap: 10px;\n"])));
var SelectDataVerioning_styles_Icon = styled_components_browser_esm["b" /* default */].img(SelectDataVerioning_styles_templateObject5 || (SelectDataVerioning_styles_templateObject5 = taggedTemplateLiteral_default()(["\n"])));
var SelectDataVerioning_styles_TableContainer = styled_components_browser_esm["b" /* default */].div(SelectDataVerioning_styles_templateObject6 || (SelectDataVerioning_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    max-width: 80vw;\n    overflow: auto;\n"])));
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/useTable.tsx

function useTable_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function useTable_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? useTable_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : useTable_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }









var SelectDataVerioning_useTable_useTable = function useTable(selected_version_task_exe_id, saveForm) {
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  var setVersioningData = Object(react["useCallback"])(function (data) {
    var version_datetime = new Date(data.version_datetime);
    saveForm({
      selected_version_task_name: data.version_name,
      selected_version_succeeded_entities: data.num_of_succeeded_entities,
      selected_version_datetime: moment_default()(version_datetime).format('YYYYMMDDHHmmss'),
      selected_version_task_exe_id: data.task_execution_id
    });
  }, [saveForm]);
  var columnsDef = Object(react["useMemo"])(function () {
    return [{
      column: 'version_name',
      name: 'Version Name',
      clickAble: true
    }, {
      column: 'task_id',
      name: 'Task Id',
      clickAble: false
    }, {
      column: 'task_execution_id',
      name: 'Task Execution Id',
      clickAble: false
    }, {
      column: 'version_no',
      name: 'Version Number',
      clickAble: false
    }, {
      column: 'execution_note',
      name: 'Execution Note',
      clickAble: false
    }, {
      column: 'task_last_updated_by',
      name: 'Last Updated By',
      clickAble: false
    }, {
      column: 'version_type',
      name: 'Version Type',
      clickAble: false
    }, {
      column: 'version_datetime',
      name: 'Creation date',
      type: 'date',
      clickAble: false
    }, {
      column: 'lu_name',
      name: 'Logical unit Name',
      clickAble: false
    }, {
      column: 'number_of_extracted_entities',
      name: 'Number of Processed Entities',
      clickAble: false
    }, {
      column: 'num_of_succeeded_entities',
      name: 'Number of Succeeded Entities',
      clickAble: false
    }, {
      column: 'num_of_failed_entities',
      name: 'Number of Failed Entities',
      clickAble: false
    }
    // {
    //     column: 'rootIndicator',
    //     name: 'Root LU',
    //     clickAble: false
    // },
    ];
  }, []);
  var columns = Object(react["useMemo"])(function () {
    var columnsResult = [];
    columnsResult.push({
      id: 'collapse',
      header: '',
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
            onClick: row.getToggleExpandedHandler(),
            style: {
              cursor: 'pointer'
            },
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectDataVerioning_styles_Icon, {
              style: {
                padding: '7px'
              },
              src: row.getIsExpanded() ? arrow_up : arrow_down
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsResult.push({
      id: 'select',
      header: '',
      cell: function cell(_ref2) {
        var row = _ref2.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: function onChange() {
              return setVersioningData(row.original);
            },
            name: "select_version_for_load",
            value: '' + row.original.task_execution_id,
            selectedValue: '' + selected_version_task_exe_id,
            title: ''
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsDef.forEach(function (col) {
      columnsResult.push(useTable_objectSpread(useTable_objectSpread({}, columnHelper.accessor(col.column, {
        header: function header() {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: col.name
          });
        },
        cell: function cell(info) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: info.getValue()
          });
        }
      })), {}, {
        width: 'auto'
      }));
    });
    return columnsResult;
  }, [columnHelper, selected_version_task_exe_id, columnsDef]);
  return {
    columns: columns
  };
};
/* harmony default export */ var SelectDataVerioning_useTable = (SelectDataVerioning_useTable_useTable);
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/groupVersions.ts

function groupVersions_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function groupVersions_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? groupVersions_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : groupVersions_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
var groupData = function groupData(data, selectedLus, allLus) {
  var lusTemp = [];
  (selectedLus || []).forEach(function (lu) {
    var luTemp = allLus.find(function (it) {
      return it.lu_name === lu;
    });
    if (luTemp) {
      lusTemp.push(groupVersions_objectSpread(groupVersions_objectSpread({}, luTemp), {}, {
        lu_parent_name: luTemp.lu_parent_name
      }));
    }
  });
  var rootLUs = lusTemp.filter(function (it) {
    return !it.lu_parent_name;
  });
  var groupedData = data.reduce(function (acc, curr) {
    if (!acc[curr.task_execution_id]) acc[curr.task_execution_id] = []; //If this type wasn't previously stored
    acc[curr.task_execution_id].push(curr);
    return acc;
  }, {});
  var versionsForLoadRoot = [];
  var versionsForLoadSubRoot = [];
  Object.keys(groupedData).forEach(function (task_execution_id) {
    groupedData[task_execution_id].forEach(function (versionForLoad) {
      if (rootLUs.findIndex(function (it) {
        return it.lu_name === versionForLoad.lu_name;
      }) >= 0 && versionsForLoadRoot.findIndex(function (it) {
        return it.task_execution_id === versionForLoad.task_execution_id;
      }) < 0) {
        versionForLoad.rootIndicator = true;
        versionsForLoadRoot.push(versionForLoad);
      } else {
        versionForLoad.rootIndicator = false;
        versionsForLoadSubRoot.push(versionForLoad);
      }
    });
  });
  var versionsForLoad = [].concat(versionsForLoadRoot);
  versionsForLoadSubRoot.forEach(function (versionForLoad) {
    var foundVersion = versionsForLoad.find(function (it) {
      return it.task_execution_id === versionForLoad.task_execution_id;
    });
    if (foundVersion) {
      if (!foundVersion.subRows) {
        foundVersion.subRows = [];
      }
      foundVersion.subRows.push(versionForLoad);
    }
  });
  return versionsForLoad;
};
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/index.tsx













function SelectDataVersioning(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    allLogicalUnits = _useContext.allLogicalUnits;
  var be_id = taskData.be_id,
    selected_logical_units_names = taskData.selected_logical_units_names,
    source_environment_name = taskData.source_environment_name,
    environment_name = taskData.environment_name,
    selection_method = taskData.selection_method,
    selection_param_value = taskData.selection_param_value,
    versioningStartDate = taskData.versioningStartDate,
    versioningEndDate = taskData.versioningEndDate,
    selected_version_task_exe_id = taskData.selected_version_task_exe_id,
    filterout_reserved = taskData.filterout_reserved,
    clone_ind = taskData.clone_ind,
    replace_sequences = taskData.replace_sequences,
    load_entity = taskData.load_entity,
    target_env = taskData.target_env,
    environment_id = taskData.environment_id,
    sync_mode = taskData.sync_mode,
    version_ind = taskData.version_ind;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    data = _useState2[0],
    setData = _useState2[1];
  var _useState3 = Object(react["useState"])(true),
    _useState4 = slicedToArray_default()(_useState3, 2),
    loading = _useState4[0],
    setLoading = _useState4[1];
  var _useTable = SelectDataVerioning_useTable(selected_version_task_exe_id, saveForm),
    columns = _useTable.columns;
  console.log(taskData);
  Object(react["useEffect"])(function () {
    var fetchData = setTimeout( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
      var local_filterout_reserved, _data, newData, sortedData;
      return regenerator_default.a.wrap(function _callee$(_context) {
        while (1) switch (_context.prev = _context.next) {
          case 0:
            _context.prev = 0;
            if (!(!versioningStartDate || !versioningStartDate || !selected_logical_units_names || !source_environment_name && !environment_name || !be_id)) {
              _context.next = 3;
              break;
            }
            return _context.abrupt("return");
          case 3:
            setLoading(true);
            local_filterout_reserved = filterout_reserved || 'OTHERS';
            if ((clone_ind || replace_sequences) && !load_entity || target_env === 'ai_training' || !environment_id || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL') {
              local_filterout_reserved = 'NA';
            }
            console.log(local_filterout_reserved);
            _context.next = 9;
            return apis_task.getVersionsForLoad(versioningStartDate, versioningEndDate, selection_method === 'ALL' ? '' : selection_param_value || '', selected_logical_units_names, source_environment_name, environment_name, be_id, local_filterout_reserved);
          case 9:
            _data = _context.sent;
            newData = groupData(_data.ListOfVersions, selected_logical_units_names, allLogicalUnits);
            sortedData = newData.sort(function (it1, it2) {
              return new Date(it2.version_datetime).getTime() - new Date(it1.version_datetime).getTime();
            });
            setData(sortedData);
            setLoading(false);
            _context.next = 19;
            break;
          case 16:
            _context.prev = 16;
            _context.t0 = _context["catch"](0);
            // use hook toast
            setLoading(false);
          case 19:
          case "end":
            return _context.stop();
        }
      }, _callee, null, [[0, 16]]);
    })), 500);
    return function () {
      return clearTimeout(fetchData);
    };
  }, [selection_param_value, selection_method, versioningStartDate, versioningEndDate, source_environment_name, environment_name, allLogicalUnits, selected_logical_units_names, be_id, filterout_reserved]);
  Object(react["useEffect"])(function () {
    var updateData = {};
    if (!versioningStartDate) {
      updateData.versioningStartDate = new Date(Date.now() - 2592000000);
    }
    if (!versioningEndDate) {
      updateData.versioningEndDate = new Date();
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
  }, [saveForm]);
  var startDateUpdate = Object(react["useCallback"])(function (startDate) {
    var updateData = {
      versioningStartDate: startDate
    };
    if (startDate && versioningEndDate && startDate > versioningEndDate) {
      updateData.versioningEndDate = new Date(startDate.getTime() + 2592000000);
    }
    saveForm(updateData);
  }, [saveForm, versioningEndDate]);
  var endDateUpdate = Object(react["useCallback"])(function (endDate) {
    saveForm({
      versioningEndDate: endDate
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(SelectDataVerioning_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
      title: 'Select version For load',
      startDate: versioningStartDate,
      startDateChange: startDateUpdate,
      endDate: versioningEndDate,
      endDateChange: endDateUpdate
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectDataVerioning_styles_TableContainer, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Table, {
        columns: columns,
        data: data,
        isExpandable: true
      })
    })]
  });
}
/* harmony default export */ var SelectDataVerioning = (SelectDataVersioning);
// CONCATENATED MODULE: ./src/components/SelectGeneratedExecution/styles.ts

var SelectGeneratedExecution_styles_templateObject, SelectGeneratedExecution_styles_templateObject2, SelectGeneratedExecution_styles_templateObject3, SelectGeneratedExecution_styles_templateObject4, SelectGeneratedExecution_styles_templateObject5, SelectGeneratedExecution_styles_templateObject6;

var SelectGeneratedExecution_styles_Container = styled_components_browser_esm["b" /* default */].div(SelectGeneratedExecution_styles_templateObject || (SelectGeneratedExecution_styles_templateObject = taggedTemplateLiteral_default()(["\n"])));
var SelectGeneratedExecution_styles_Title = styled_components_browser_esm["b" /* default */].div(SelectGeneratedExecution_styles_templateObject2 || (SelectGeneratedExecution_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n"])));
var SelectGeneratedExecution_styles_DatesContainer = styled_components_browser_esm["b" /* default */].div(SelectGeneratedExecution_styles_templateObject3 || (SelectGeneratedExecution_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  margin-top: 20px;\n  margin-bottom: 38px;\n  font-family: Roboto;\n  font-size: 16px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1.25;\n  letter-spacing: normal;\n  text-align: left;\n  color: #2e2e2e;\n  display: flex;\n  align-items: center;\n  gap: 30px;\n"])));
var SelectGeneratedExecution_styles_DateItem = styled_components_browser_esm["b" /* default */].div(SelectGeneratedExecution_styles_templateObject4 || (SelectGeneratedExecution_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  display: flex;\n  align-items: center;\n  gap: 10px;\n"])));
var SelectGeneratedExecution_styles_TableContainer = styled_components_browser_esm["b" /* default */].div(SelectGeneratedExecution_styles_templateObject5 || (SelectGeneratedExecution_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    max-width: 80vw;\n    overflow: auto;\n"])));
var SelectGeneratedExecution_styles_Icon = styled_components_browser_esm["b" /* default */].img(SelectGeneratedExecution_styles_templateObject6 || (SelectGeneratedExecution_styles_templateObject6 = taggedTemplateLiteral_default()(["\n"])));
// CONCATENATED MODULE: ./src/components/SelectGeneratedExecution/useTable.tsx

function SelectGeneratedExecution_useTable_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function SelectGeneratedExecution_useTable_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? SelectGeneratedExecution_useTable_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : SelectGeneratedExecution_useTable_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }









var SelectGeneratedExecution_useTable_useTable = function useTable() {
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var selected_subset_task_exe_id = taskData.selected_subset_task_exe_id;
  var generationIdChange = Object(react["useCallback"])(function (selected_subset_task_exe_id, num_of_entities) {
    saveForm({
      selected_subset_task_exe_id: selected_subset_task_exe_id,
      num_of_entities: num_of_entities
    });
  }, [saveForm]);
  var columnsDef = Object(react["useMemo"])(function () {
    return [{
      column: 'task_title',
      name: 'Generation task name'
    }, {
      column: 'lu_name',
      name: 'Logical unit Name'
    }, {
      column: 'task_execution_id',
      name: 'Task execution id',
      clickAble: false
    }, {
      column: 'execution_note',
      name: 'Execution note',
      clickAble: false
    }, {
      column: 'start_execution_time',
      name: 'Execution Time',
      clickAble: false
    }, {
      column: 'number_of_entities',
      name: 'Number of generated entities',
      clickAble: false
    }];
  }, []);
  var columns = Object(react["useMemo"])(function () {
    var columnsResult = [];
    columnsResult.push({
      id: 'collapse',
      header: '',
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
            onClick: row.getToggleExpandedHandler(),
            style: {
              cursor: 'pointer'
            },
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectGeneratedExecution_styles_Icon, {
              style: {
                padding: '7px'
              },
              src: row.getIsExpanded() ? arrow_up : arrow_down
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsResult.push({
      id: 'select',
      header: '',
      cell: function cell(_ref2) {
        var row = _ref2.row;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: function onChange() {
              return generationIdChange(row.original.task_execution_id, row.original.number_of_entities);
            },
            name: "select_generation_execution",
            value: row.original.task_execution_id,
            selectedValue: selected_subset_task_exe_id,
            title: ''
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsDef.forEach(function (col) {
      columnsResult.push(SelectGeneratedExecution_useTable_objectSpread(SelectGeneratedExecution_useTable_objectSpread({}, columnHelper.accessor(col.column, {
        header: function header() {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: col.name
          });
        },
        cell: function cell(info) {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: info.getValue()
          });
        }
      })), {}, {
        width: 'auto'
      }));
    });
    return columnsResult;
  }, [columnHelper, columnsDef, selected_subset_task_exe_id, generationIdChange]);
  return {
    columns: columns
  };
};
/* harmony default export */ var SelectGeneratedExecution_useTable = (SelectGeneratedExecution_useTable_useTable);
// CONCATENATED MODULE: ./src/components/SelectGeneratedExecution/index.tsx













function SelectGeneratedExecution(props) {
  var dataSourceType = props.dataSourceType;
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    allLogicalUnits = _useContext.allLogicalUnits;
  var generationStartDate = taskData.generationStartDate,
    generationEndDate = taskData.generationEndDate,
    source_environment_name = taskData.source_environment_name,
    be_id = taskData.be_id,
    selected_logical_units_names = taskData.selected_logical_units_names,
    onReset = taskData.onReset;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    data = _useState2[0],
    setData = _useState2[1];
  var _useState3 = Object(react["useState"])(true),
    _useState4 = slicedToArray_default()(_useState3, 2),
    loading = _useState4[0],
    setLoading = _useState4[1];
  var _useTable = SelectGeneratedExecution_useTable(),
    columns = _useTable.columns;
  Object(react["useEffect"])(function () {
    console.log("onReset=".concat(onReset));
    if (onReset) {
      var updateData = {};
      updateData.generationStartDate = new Date(Date.now() - 2592000000);
      updateData.generationEndDate = new Date();
      if (Object.keys(updateData).length > 0) {
        saveForm(updateData);
      }
    }
  }, [onReset]);
  Object(react["useEffect"])(function () {
    var fetchData = setTimeout( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
      var _data, newData, sortedData;
      return regenerator_default.a.wrap(function _callee$(_context) {
        while (1) switch (_context.prev = _context.next) {
          case 0:
            _context.prev = 0;
            if (!(!generationStartDate || !generationEndDate || !source_environment_name || !be_id || (selected_logical_units_names === null || selected_logical_units_names === void 0 ? void 0 : selected_logical_units_names.length) === 0)) {
              _context.next = 3;
              break;
            }
            return _context.abrupt("return");
          case 3:
            setLoading(true);
            _context.next = 6;
            return apis_task.getGenerationExecutions(generationStartDate, generationEndDate, source_environment_name, be_id, selected_logical_units_names);
          case 6:
            _data = _context.sent;
            newData = groupData(_data, selected_logical_units_names || [], allLogicalUnits);
            sortedData = newData.sort(function (it1, it2) {
              return new Date(it2.start_execution_time).getTime() - new Date(it1.start_execution_time).getTime();
            });
            setData(sortedData);
            setLoading(false);
            _context.next = 16;
            break;
          case 13:
            _context.prev = 13;
            _context.t0 = _context["catch"](0);
            // use hook toast
            setLoading(false);
          case 16:
          case "end":
            return _context.stop();
        }
      }, _callee, null, [[0, 13]]);
    })), 500);
    return function () {
      return clearTimeout(fetchData);
    };
  }, [generationStartDate, generationEndDate, source_environment_name, be_id, selected_logical_units_names, allLogicalUnits]);
  Object(react["useEffect"])(function () {
    var updateData = {};
    if (!generationStartDate) {
      updateData.generationStartDate = new Date(Date.now() - 2592000000);
    }
    if (!generationEndDate) {
      updateData.generationEndDate = new Date();
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
  }, []);
  var startDateUpdate = Object(react["useCallback"])(function (startDate) {
    saveForm({
      generationStartDate: startDate
    });
  }, [saveForm]);
  var endDateUpdate = Object(react["useCallback"])(function (endDate) {
    saveForm({
      generationEndDate: endDate
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(SelectGeneratedExecution_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
      title: 'Select Generation',
      startDate: generationStartDate,
      startDateChange: startDateUpdate,
      endDate: generationEndDate,
      endDateChange: endDateUpdate
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectGeneratedExecution_styles_TableContainer, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Table, {
        columns: columns,
        data: data,
        isExpandable: true
      })
    })]
  });
}
/* harmony default export */ var components_SelectGeneratedExecution = (SelectGeneratedExecution);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Parameters/styles.ts

var Parameters_styles_templateObject, Parameters_styles_templateObject2, Parameters_styles_templateObject3, Parameters_styles_templateObject4, Parameters_styles_templateObject5, Parameters_styles_templateObject6, Parameters_styles_templateObject7, Parameters_styles_templateObject8, Parameters_styles_templateObject9, Parameters_styles_templateObject10, Parameters_styles_templateObject11, Parameters_styles_templateObject12;

var rotateAnimation = Object(styled_components_browser_esm["c" /* keyframes */])(Parameters_styles_templateObject || (Parameters_styles_templateObject = taggedTemplateLiteral_default()(["\n100% { -webkit-transform: rotate(360deg); } \n"])));
var Parameters_styles_Container = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject2 || (Parameters_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    min-width: 60vw;\n    position: relative;\n    display: block;\n    border-top: 1px solid #ccc;\n    padding-top: 15px;\n"])));
var Parameters_styles_Icon = styled_components_browser_esm["b" /* default */].img(Parameters_styles_templateObject3 || (Parameters_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    width: 27px;\n"])));
var AnimationIcon = styled_components_browser_esm["b" /* default */].img(Parameters_styles_templateObject4 || (Parameters_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    width: 27px;\n    animation: ", " 3s linear infinite\n"])), rotateAnimation);
var DateFormatNote = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject5 || (Parameters_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n"])));
var FilterOutReservedContainer = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject6 || (Parameters_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    position: relative;\n    display: flex;\n    gap: 10px;\n    flex-direction: column;\n"])));
var SQLQuery = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject7 || (Parameters_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n    margin-top: 30px;\n"])));
var MaxNumberOfEntitiesContainer = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject8 || (Parameters_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    margin-top: 88px;\n"])));
var Parameters_styles_LeftSide = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject9 || (Parameters_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    gap: 10px;\n    width: 400px;\n    border-right: 1px solid #ccc;\n"])));
var RefreshParameters = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject10 || (Parameters_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    margin-bottom: 23px;\n    display: flex;\n    align-items: center;\n    gap: 12px;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bolder;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n"])));
var Parameters_styles_Seprator = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject11 || (Parameters_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    border-right: 1px solid #ccc;\n    width: 1px;\n    position: absolute;\n    height: calc(100% + 80px);\n    top: 0px;\n    left: 400px;\n"])));
var Parameters_styles_RightSide = styled_components_browser_esm["b" /* default */].div(Parameters_styles_templateObject12 || (Parameters_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    margin-top: 15px;\n"])));
// CONCATENATED MODULE: ./src/components/QueryBuilder/styles.ts

var QueryBuilder_styles_templateObject, QueryBuilder_styles_templateObject2, QueryBuilder_styles_templateObject3, QueryBuilder_styles_templateObject4, QueryBuilder_styles_templateObject5, QueryBuilder_styles_templateObject6, QueryBuilder_styles_templateObject7;

var QueryBuilder_styles_Container = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject || (QueryBuilder_styles_templateObject = taggedTemplateLiteral_default()(["\n    padding: 30px;\n    border: 2px solid #cccccc;\n    max-width: 100%;\n    margin-top: 10px;\n    background-color: #f9f9f9;\n"])));
var RulesContainer = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject2 || (QueryBuilder_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
var RuleContainer = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject3 || (QueryBuilder_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    position: relative;\n"])));
var ActionsContainer = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject4 || (QueryBuilder_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 10px;\n    margin-bottom: 13px;\n"])));
var RuleItemContainer = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject5 || (QueryBuilder_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 10px;\n    align-items: center;\n    height: 45px;\n    margin-bottom: 9px;\n"])));
var MinMaxNote = styled_components_browser_esm["b" /* default */].div(QueryBuilder_styles_templateObject6 || (QueryBuilder_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n    align-self: center;\n    margin-top: 3px;\n"])));
var RemoveRuleIcon = styled_components_browser_esm["b" /* default */].img(QueryBuilder_styles_templateObject7 || (QueryBuilder_styles_templateObject7 = taggedTemplateLiteral_default()(["\n margin-left: auto;\n"])));
// CONCATENATED MODULE: ./src/components/Button/styles.ts

var Button_styles_templateObject, Button_styles_templateObject2, Button_styles_templateObject3;

var Button_styles_Container = styled_components_browser_esm["b" /* default */].div(Button_styles_templateObject || (Button_styles_templateObject = taggedTemplateLiteral_default()(["\n    width:  ", ";\n    height: ", ";\n    object-fit: contain;\n    border-radius: 3px;\n    background-color: ", ";\n    display: flex;\n    gap: 9px;\n    justify-content: center;\n    align-items: center;\n    cursor: pointer;\n    border: ", ";\n"])), function (props) {
  return props.width || '100%';
}, function (props) {
  return props.height || '35px';
}, function (props) {
  if (props.backgroundColor) {
    return props.backgroundColor;
  }
  if (props.type === 'secondary') {
    return '#fff';
  }
  return 'var(--primary-color)';
}, function (props) {
  if (props.danger) {
    return 'solid 1px #ff6666';
  }
  if (props.type === 'secondary') {
    return 'solid 1px #1483f3';
  }
  return '0';
});
var Button_styles_Title = styled_components_browser_esm["b" /* default */].div(Button_styles_templateObject2 || (Button_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    gap: 11px;\n    font-family: Roboto;\n    font-size: 15px;\n    font-weight: bolder;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: center;\n    color: ", "; \n"])), function (props) {
  if (props.danger) {
    return '#ff6666';
  }
  if (props.type === 'secondary') {
    return 'var(--primary-color)';
  }
  return '#fefefe';
});
var Button_styles_Icon = styled_components_browser_esm["b" /* default */].img(Button_styles_templateObject3 || (Button_styles_templateObject3 = taggedTemplateLiteral_default()(["\n"])));
// CONCATENATED MODULE: ./src/components/Button/index.tsx




function Button(props) {
  var title = props.title,
    onClick = props.onClick,
    width = props.width,
    height = props.height,
    type = props.type,
    disabled = props.disabled,
    icon = props.icon,
    danger = props.danger,
    backgroundColor = props.backgroundColor,
    children = props.children;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Button_styles_Container, {
    backgroundColor: backgroundColor,
    onClick: onClick,
    height: height,
    width: width,
    type: type,
    danger: danger,
    children: [icon ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Button_styles_Icon, {
      src: icon
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(Button_styles_Title, {
      type: type,
      danger: danger,
      children: children ? children : title
    })]
  });
}
/* harmony default export */ var components_Button = (Button);
// CONCATENATED MODULE: ./src/components/QueryBuilder/useConstants.tsx

var useConstants_useConstants = function useConstants() {
  var operators = Object(react["useMemo"])(function () {
    return [{
      label: 'AND',
      name: 'AND',
      value: 'AND'
    }, {
      label: 'OR',
      name: 'OR',
      value: 'OR'
    }];
  }, []);
  var conditions = Object(react["useMemo"])(function () {
    return [{
      name: '=',
      label: '=',
      value: '=',
      tableValue: '='
    }, {
      name: '<>',
      label: '<>',
      value: '<>',
      tableValue: '<>'
    }, {
      name: '>',
      label: '>',
      value: '<',
      tableValue: '>'
    }, {
      name: '>=',
      label: '>=',
      value: '<=',
      tableValue: '>='
    }, {
      name: '<',
      label: '<',
      value: '>',
      tableValue: '<'
    }, {
      name: '<=',
      label: '<=',
      value: '>=',
      tableValue: '<='
    }, {
      name: 'IS NULL',
      label: 'IS NULL',
      value: 'IS NULL',
      tableValue: 'IS NULL'
    }, {
      name: 'IS NOT NULL',
      label: 'IS NOT NULL',
      value: 'IS NOT NULL',
      tableValue: 'IS NOT NULL'
    }, {
      name: 'IN',
      label: 'IN',
      value: 'IN',
      multiple: true,
      tableValue: 'IN'
    }, {
      name: 'NOT IN',
      label: 'NOT IN',
      value: 'NOT IN',
      multiple: true,
      tableValue: 'NOT IN'
    }];
  }, []);
  var comboConditions = Object(react["useMemo"])(function () {
    return [{
      name: '=',
      label: '=',
      value: '=',
      tableValue: '='
    }, {
      name: '<>',
      label: '<>',
      value: '<>',
      tableValue: '<>'
    }, {
      name: '>',
      label: '>',
      value: '<',
      tableValue: '>'
    }, {
      name: '>=',
      label: '>=',
      value: '<=',
      tableValue: '>='
    }, {
      name: '<',
      label: '<',
      value: '>',
      tableValue: '<'
    }, {
      name: '<=',
      label: '<=',
      value: '>=',
      tableValue: '<='
    }, {
      name: 'IN',
      label: 'IN',
      value: 'IN',
      multiple: true,
      tableValue: 'IN'
    }, {
      name: 'NOT IN',
      label: 'NOT IN',
      value: 'NOT IN',
      multiple: true,
      tableValue: 'NOT IN'
    }, {
      name: 'IS NULL',
      label: 'IS NULL',
      value: 'IS NULL',
      tableValue: 'IS NULL'
    }, {
      name: 'IS NOT NULL',
      label: 'IS NOT NULL',
      value: 'IS NOT NULL',
      tableValue: 'IS NOT NULL'
    }];
  }, []);
  var dateConditions = Object(react["useMemo"])(function () {
    return [{
      name: '=',
      label: '=',
      value: '=',
      tableValue: '='
    }, {
      name: '<>',
      label: '<>',
      value: '<>',
      tableValue: '<>'
    }, {
      name: '>',
      label: '>',
      value: '<',
      tableValue: '>'
    }, {
      name: '>=',
      label: '>=',
      value: '<=',
      tableValue: '>='
    }, {
      name: '<',
      label: '<',
      value: '>',
      tableValue: '<'
    }, {
      name: '<=',
      label: '<=',
      value: '>=',
      tableValue: '<='
    }];
  }, []);
  return {
    operators: operators,
    conditions: conditions,
    comboConditions: comboConditions,
    dateConditions: dateConditions
  };
};
/* harmony default export */ var QueryBuilder_useConstants = (useConstants_useConstants);
// CONCATENATED MODULE: ./src/components/QueryBuilder/Rule.tsx












function Rule(props) {
  var rule = props.rule,
    parameters = props.parameters,
    ruleIndex = props.ruleIndex,
    groupIndex = props.groupIndex,
    lastRule = props.lastRule,
    onChange = props.onChange,
    parentGroup = props.parentGroup,
    type = props.type;
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    config_params = _useContext.config_params;
  var _useState = Object(react["useState"])(),
    _useState2 = slicedToArray_default()(_useState, 2),
    chosenParam = _useState2[0],
    setChosenParam = _useState2[1];
  var _useState3 = Object(react["useState"])(),
    _useState4 = slicedToArray_default()(_useState3, 2),
    chosenCondition = _useState4[0],
    setChosenCondition = _useState4[1];
  var _useState5 = Object(react["useState"])(),
    _useState6 = slicedToArray_default()(_useState5, 2),
    chosenOperator = _useState6[0],
    setChosenOperator = _useState6[1];
  var _useState7 = Object(react["useState"])([]),
    _useState8 = slicedToArray_default()(_useState7, 2),
    currentConditions = _useState8[0],
    setCurrentConditions = _useState8[1];
  var _useConstants = QueryBuilder_useConstants(),
    operators = _useConstants.operators,
    conditions = _useConstants.conditions,
    comboConditions = _useConstants.comboConditions,
    dateConditions = _useConstants.dateConditions;
  Object(react["useEffect"])(function () {
    if (!rule.field) {
      setChosenParam(undefined);
      setCurrentConditions([]);
      return;
    }
    var currentParam = parameters === null || parameters === void 0 ? void 0 : parameters.find(function (it) {
      return it.param_name === rule.field;
    });
    setChosenParam(currentParam);
    if (currentParam) {
      var condToUse = [];
      switch (currentParam.param_type) {
        case 'NUMBER':
        case 'INTEGER':
        case 'REAL':
          condToUse = conditions;
          break;
        case 'TEXT':
        case 'DATETIME':
        case 'DATE':
        case 'TIME':
          condToUse = comboConditions;
          if (currentParam.table_filter) {
            condToUse = conditions;
          }
          break;
        default:
          condToUse = [];
          break;
      }
      if (type === 1) {
        condToUse = condToUse.map(function (it) {
          var newItem = Object.assign({}, it);
          newItem.value = newItem.tableValue;
          return newItem;
        });
      }
      setCurrentConditions(condToUse);
    }
  }, [comboConditions, conditions, dateConditions, parameters, rule.field, type]);
  Object(react["useEffect"])(function () {
    setChosenCondition(currentConditions.find(function (it) {
      return it.value === (rule === null || rule === void 0 ? void 0 : rule.condition);
    }));
  }, [rule.condition, currentConditions]);
  Object(react["useEffect"])(function () {
    setChosenOperator(operators.find(function (it) {
      return it.value === (rule === null || rule === void 0 ? void 0 : rule.operator);
    }));
  }, [rule.operator]);
  var onParamChange = Object(react["useCallback"])(function (item) {
    // setChosenParam(item);
    rule.field = item === null || item === void 0 ? void 0 : item.param_name;
    rule.type = item === null || item === void 0 ? void 0 : item.param_type;
    rule.original_type = item === null || item === void 0 ? void 0 : item.original_type;
    rule.validValues = (item === null || item === void 0 ? void 0 : item.valid_values) || [];
    rule.table = item === null || item === void 0 ? void 0 : item.table;
    rule.data = null;
    onChange();
    //TODO save up
  }, [onChange, rule]);
  var onConditionChange = Object(react["useCallback"])(function (item) {
    // setChosenCondition(item);
    rule.condition = item.value;
    rule.data = null;
    onChange();
    //TODO save up
  }, [onChange, rule]);
  var onOperatorChange = Object(react["useCallback"])(function (item) {
    rule.operator = item.value;
    setChosenOperator(item);
    onChange();
    //TODO save up
  }, [rule, onChange]);
  var onComboValueChange = Object(react["useCallback"])(function (value) {
    if (Array.isArray(value)) {
      rule.data = value.map(function (it) {
        return it.value;
      });
    } else {
      rule.data = value.value;
    }
    onChange();
  }, [rule, onChange]);
  var onValueChange = Object(react["useCallback"])(function (value) {
    rule.data = value;
    onChange();
  }, [rule, onChange]);
  var getRuleByType = Object(react["useCallback"])(function () {
    if (!rule.field || !chosenCondition || rule.condition === 'IS NULL' || rule.condition === 'IS NOT NULL') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
    }
    if (chosenParam !== null && chosenParam !== void 0 && chosenParam.COMBO_INDICATOR) {
      var tempValue = undefined;
      if (Array.isArray(rule.data)) {
        tempValue = chosenParam.valid_values.filter(function (it) {
          return rule.data.indexOf(it.value) >= 0;
        });
      } else {
        tempValue = chosenParam.valid_values.find(function (it) {
          return rule.data === it.value;
        });
      }
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        width: '300px',
        isMulti: chosenCondition.multiple,
        title: '',
        mandatory: true,
        options: chosenParam.valid_values,
        value: tempValue,
        onChange: onComboValueChange
      });
    } else if ((chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.param_type) === 'NUMBER' || (chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.param_type) === 'REAL' || (chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.param_type) === 'INTEGER') {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
          name: "rule_number_".concat(groupIndex, "_").concat(ruleIndex),
          title: '',
          onChange: onValueChange,
          value: rule.data,
          type: InputTypes.number,
          mandatory: true,
          width: '300px',
          min: chosenParam.min_value,
          max: chosenParam.max_value
        }), chosenParam !== null && chosenParam !== void 0 && chosenParam.min_value && chosenParam !== null && chosenParam !== void 0 && chosenParam.max_value ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(MinMaxNote, {
          children: ["(Min: ", chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.min_value, " Max:", ' ', chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.max_value, ")"]
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      });
    } else {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
        name: "rule_text_".concat(groupIndex, "_").concat(ruleIndex),
        title: '',
        onChange: onValueChange,
        value: rule.data,
        type: InputTypes.text,
        mandatory: true,
        width: '300px'
      });
    }
  }, [chosenParam, rule.data, rule.field, rule.condition, chosenCondition, onValueChange, onComboValueChange]);
  var removeGroup = Object(react["useCallback"])(function (index) {
    if (!parentGroup || !parentGroup.rules || parentGroup.rules.length === 0) {
      return;
    }
    var groupIndex = -1;
    if (typeof index === 'number') {
      groupIndex = index;
    } else {
      var splittedIndex = index.split('_');
      groupIndex = splittedIndex[splittedIndex.length - 1];
    }
    if (parentGroup.rules[groupIndex]) {
      parentGroup.rules.splice(groupIndex, 1);
    }
    onChange();
  }, [onChange, parentGroup]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(RuleContainer, {
    children: [rule.hasOwnProperty('group') ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_QueryBuilder, {
      removeGroup: removeGroup,
      parent: [],
      onChange: onChange,
      index: "".concat(groupIndex, "_").concat(ruleIndex),
      group: rule.group,
      parameters: parameters
    }) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(RuleItemContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        width: config_params !== null && config_params !== void 0 && config_params.enable_param_auto_width ? 'auto' : '290px',
        minWidth: config_params !== null && config_params !== void 0 && config_params.enable_param_auto_width ? '290px' : '',
        maxWidth: config_params !== null && config_params !== void 0 && config_params.enable_param_auto_width ? '500px' : '',
        title: '',
        mandatory: true,
        options: parameters,
        value: chosenParam,
        isClearable: true,
        onChange: onParamChange
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        width: '160px',
        title: '',
        mandatory: true,
        options: currentConditions,
        value: chosenCondition,
        onChange: onConditionChange
      }), getRuleByType(), /*#__PURE__*/Object(jsx_runtime["jsx"])(RemoveRuleIcon, {
        onClick: function onClick() {
          return removeGroup(ruleIndex);
        },
        src: delete_icon_gray
      })]
    }), lastRule ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
      width: '100px',
      title: '',
      mandatory: true,
      options: operators,
      value: chosenOperator,
      onChange: onOperatorChange
    })]
  });
}
/* harmony default export */ var QueryBuilder_Rule = (Rule);
// CONCATENATED MODULE: ./src/images/plus.svg
/* harmony default export */ var plus = ("js/dist/1f5263d2bddd3f4ad9a3bb4a37bb816b.svg");
// CONCATENATED MODULE: ./src/components/QueryBuilder/index.tsx










function QueryBuilder(props) {
  var _group$rules;
  var parent = props.parent,
    group = props.group,
    parameters = props.parameters,
    index = props.index,
    onChange = props.onChange,
    removeGroup = props.removeGroup,
    type = props.type;
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors;
  var getRule = function getRule(parentGroup, rule, ruleIndex, lastRule) {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(QueryBuilder_Rule, {
      type: type,
      parentGroup: parentGroup,
      onChange: onChange,
      lastRule: lastRule,
      groupIndex: index,
      ruleIndex: ruleIndex,
      rule: rule,
      parameters: parameters
    });
  };
  var addCondition = Object(react["useCallback"])(function () {
    group === null || group === void 0 ? void 0 : group.rules.push({
      condition: '',
      field: '',
      data: null,
      operator: 'AND',
      validValues: []
    });
    onChange();
  }, [onChange]);
  var addGroup = Object(react["useCallback"])(function () {
    group === null || group === void 0 ? void 0 : group.rules.push({
      group: {
        operator: 'AND',
        rules: []
      }
    });
    onChange();
  }, [onChange]);
  var removeGroupLocal = Object(react["useCallback"])(function () {
    if (removeGroup) {
      removeGroup(index);
    }
  }, [removeGroup]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(QueryBuilder_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionsContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
        title: 'Add condition',
        type: 'secondary',
        width: '150px',
        onClick: addCondition,
        backgroundColor: "trasnparent",
        icon: plus
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
        title: 'Add group',
        type: 'secondary',
        width: '150px',
        onClick: addGroup,
        backgroundColor: "trasnparent",
        icon: plus
      }), removeGroup ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
        title: 'Remove group',
        width: '150px',
        type: 'secondary',
        danger: true,
        backgroundColor: "trasnparent",
        onClick: removeGroupLocal,
        icon: delete_icon_gray
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(RulesContainer, {
      children: group === null || group === void 0 ? void 0 : (_group$rules = group.rules) === null || _group$rules === void 0 ? void 0 : _group$rules.map(function (rule, index) {
        var _group$rules2;
        return getRule(group, rule, index, index === (group === null || group === void 0 ? void 0 : (_group$rules2 = group.rules) === null || _group$rules2 === void 0 ? void 0 : _group$rules2.length) - 1);
      })
    })]
  });
}
/* harmony default export */ var components_QueryBuilder = (QueryBuilder);
// CONCATENATED MODULE: ./src/images/refresh.svg
/* harmony default export */ var refresh = ("js/dist/3422139b10e662c2a4e764661a8a8ea4.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Parameters/utils.tsx
var getSubWhereInNotIn = function getSubWhereInNotIn(param, condition, value, type) {
  if (!value || value.length === 0) {
    return '';
  }
  if (!Array.isArray(value)) {
    value = [value];
  }
  var operator = 'or';
  var equality = '=';
  if (condition !== 'IN') {
    operator = 'and';
    equality = '!=';
  }
  var result = '';
  for (var i = 0; i < value.length; i++) {
    if (type === 1) {
      result = result + "".concat(param, " ").concat(equality, " ").concat(value[i]);
    } else {
      result = result + "'".concat(value[i], "' ").concat(equality, " ANY(").concat(param, ")");
    }
    if (i < value.length - 1) {
      result = " ".concat(result, " ").concat(operator, " ");
    }
  }
  return result;
};
var getSubQuery = function getSubQuery(rule, parameters, type, resultValues, filter_types) {
  if (!rule) {
    return '';
  }
  var field = '"' + rule.field + '"';
  if (type === 1) {
    field = rule.field || '';
  }
  var condition = rule.condition;
  var data = rule.data;
  if (type === 1) {
    if (rule.condition !== 'IS NULL' && rule.condition !== 'IS NOT NULL') {
      if (rule.original_type === 'TEXT') {
        data = data.replace(/\'/g, "''");
      }
      if (rule.condition === 'IN' || rule.condition === 'NOT IN') {
        var values = data.split(',').map(function (it) {
          return it.trim();
        });
        data = [];
        values.forEach(function (value) {
          data.push('?');
          resultValues === null || resultValues === void 0 ? void 0 : resultValues.push(value);
          filter_types === null || filter_types === void 0 ? void 0 : filter_types.push({
            field_name: rule.field,
            field_type: rule.original_type,
            field_value: value
          });
        });
      } else {
        resultValues === null || resultValues === void 0 ? void 0 : resultValues.push(data);
        filter_types === null || filter_types === void 0 ? void 0 : filter_types.push({
          field_name: rule.field,
          field_type: rule.original_type,
          field_value: data
        });
        data = '?';
      }
    } else {
      data = '';
    }
  } else {
    if (typeof data === 'string') {
      data = data.replace(/\'/g, "''");
    }
    data = "'" + data + "'";
    if (rule.type === "number") {
      field = field + "::numeric[] ";
      data = rule.data;
    }
  }
  var table = rule.table;
  if (!table && parameters && parameters.length > 0) {
    var found = parameters.find(function (it) {
      return it.name === rule.field;
    });
    if (found) {
      table = found.table;
    }
  }
  var prefix = "SELECT ROOT_IID FROM ".concat(table, " WHERE ");
  if (type === 1) {
    prefix = "";
  }
  if (rule.condition === 'IS NULL' || rule.condition === 'IS NOT NULL') {
    return '( ' + prefix + field + ' ' + condition + ' )';
  } else if (rule.condition === 'IN' || rule.condition === 'NOT IN') {
    if (type !== 1) {
      data = rule.data;
    }
    if (Array.isArray(data)) {
      var newData = [];
      data.forEach(function (it) {
        var value = it;
        if (typeof value === 'string') {
          value = value.replace(/\'/g, "''");
        }
        newData.push(value);
      });
      data = newData;
    } else if (typeof data === 'string') {
      data = data.split(',').map(function (it) {
        return it.trim();
      });
    }
    return "( ".concat(prefix, " ").concat(getSubWhereInNotIn(field, condition, data, type), ")");
  } else {
    if (type === 1) {
      return '( ' + prefix + field + ' ' + condition + ' ' + data + ' )';
    } else {
      return '( ' + prefix + data + ' ' + condition + ' ANY(' + field + ') )';
    }
  }
};
var computeQuery = function computeQuery(group, parametersList, type, resultValues, filter_types) {
  if (!group) return '';
  var str = '(';
  var _loop = function _loop(i) {
      if (group.rules[i].group) {
        if (i === group.rules.length - 1) {
          str += computeQuery(group.rules[i].group, parametersList, type, resultValues, filter_types);
        } else {
          str += computeQuery(group.rules[i].group, parametersList, type, resultValues, filter_types) + ' ' + (group.rules[i].operator === 'AND' ? 'INTERSECT' : 'UNION') + ' ';
        }
      } else {
        var data;
        if (!group.rules[i].data && group.rules[i].data !== '' && group.rules[i].condition !== 'IS NULL' && group.rules[i].condition !== 'IS NOT NULL') {
          return {
            v: ''
          };
        }
        if (group.rules[i].type === 'real') {
          if (group.rules[i].data.toLocaleString().indexOf('.') <= 0) {
            data = group.rules[i].data.toFixed(1);
          } else {
            data = group.rules[i].data;
          }
        } else if (group.rules[i].type === 'integer') {
          data = Math.floor(group.rules[i].data);
        } else if (group.rules[i].type === 'combo') {
          var paramFound = parametersList === null || parametersList === void 0 ? void 0 : parametersList.find(function (it) {
            return it.name === group.rules[i].field;
          });
          if (paramFound && paramFound.valid_values) {
            var validValues = paramFound.valid_values.map(function (it) {
              return it.label;
            });
            if (validValues && validValues.length > 0 && validValues.indexOf(group.rules[i].data) < 0 && group.rules[i].condition !== 'IS NULL' && group.rules[i].condition !== 'IS NOT NULL') {
              return {
                v: ''
              };
            }
          }
          data = group.rules[i].data;
        } else {
          data = group.rules[i].data;
        }
        if (!data) {
          data = '';
        }
        if (i === group.rules.length - 1) {
          str += getSubQuery(group.rules[i], parametersList, type, resultValues, filter_types);
        } else {
          str += getSubQuery(group.rules[i], parametersList, type, resultValues, filter_types);
          if (type === 1) {
            str += ' \n' + group.rules[i].operator + ' \n';
          } else {
            str += ' \n' + (group.rules[i].operator === 'AND' ? 'INTERSECT' : 'UNION') + ' \n';
          }
        }
      }
    },
    _ret;
  for (var i = 0; i < group.rules.length; i++) {
    _ret = _loop(i);
    if (_ret) return _ret.v;
  }
  return str + ')';
};
var getSelectionParamValue = function getSelectionParamValue(filter, parametersList, type) {
  var resultValues = [];
  var filter_types = [];
  var validStatement = false;
  var checkRule = function checkRule(rule, type) {
    if (rule.group) {
      return checkGroup(rule.group, type);
    } else {
      if (!rule.field || !rule.condition) {
        return false;
      } else if (['IS NULL', 'IS NOT NULL'].indexOf(rule.condition) >= 0) {
        validStatement = true;
        return true;
      } else if (['IN', 'NOT IN'].indexOf(rule.condition) >= 0) {
        if (!rule.data || rule.data.length === 0) {
          return false;
        }
      } else if (!rule.data && rule.data !== "") {
        return false;
      }
      validStatement = true;
      return true;
    }
  };
  var checkGroup = function checkGroup(group, type, first) {
    if (!group.operator) {
      return false;
    }
    for (var i = 0; i < group.rules.length; i++) {
      if (checkRule(group.rules[i], type) === false) {
        return false;
      }
    }
    if (!first && group.rules.length === 0) {
      return false;
    }
    return true;
  };
  if (filter && checkGroup(filter.group, type, true) === true) {
    var result = computeQuery(filter.group, parametersList, type, resultValues, filter_types);
    if (type === 1) {
      return {
        sqlQuery: result,
        values: resultValues,
        filter_types: filter_types
      };
    }
    return result;
  }
  return '';
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/styles.ts

var TaskMainWidget_styles_templateObject, TaskMainWidget_styles_templateObject2, TaskMainWidget_styles_templateObject3, TaskMainWidget_styles_templateObject4, TaskMainWidget_styles_templateObject5, TaskMainWidget_styles_templateObject6, TaskMainWidget_styles_templateObject7, TaskMainWidget_styles_templateObject8;

var duration = "2s forwards";
function ChangeBorderColor(property, color) {
  var animation = "to {\n    ".concat(property, ": ").concat(color, ";\n  }");
  return Object(styled_components_browser_esm["c" /* keyframes */])(TaskMainWidget_styles_templateObject || (TaskMainWidget_styles_templateObject = taggedTemplateLiteral_default()(["\n    ", "\n  "])), animation);
}
var TaskMainWidget_styles_Container = styled_components_browser_esm["b" /* default */].div(TaskMainWidget_styles_templateObject2 || (TaskMainWidget_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  width: 876px;\n  height: 277px;\n  margin: auto;\n  display: flex;\n  align-items: center;\n  justify-content: space-between;\n  user-select: none;\n  position: absolute;\n  top: -37px;\n  scale: 0.7;\n"])));
var ArrowTriangle = styled_components_browser_esm["b" /* default */].div(TaskMainWidget_styles_templateObject3 || (TaskMainWidget_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  border-top: 19px solid transparent;\n  border-bottom: 19px solid transparent;\n  border-left: 19px solid #ccc;\n    animation: ", " ", ";\n  position: absolute;\n  top: 50%;\n  left: ", ";\n  transform: translate(-50%, -50%);\n  z-index: 1;\n"])), function (_ref) {
  var color = _ref.color;
  return ChangeBorderColor('border-left-color', color || "#ccc");
}, duration, function (props) {
  return props.left;
});
var TaskMainWidget_styles_Title = styled_components_browser_esm["b" /* default */].div(TaskMainWidget_styles_templateObject4 || (TaskMainWidget_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 19px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  letter-spacing: normal;\n  text-align: center;\n  color: var(--disabled);\n  ", "\n  ", "\n\n"])), function (_ref2) {
  var color = _ref2.color;
  return color && Object(styled_components_browser_esm["a" /* css */])(TaskMainWidget_styles_templateObject5 || (TaskMainWidget_styles_templateObject5 = taggedTemplateLiteral_default()(["\n      animation: ", " ", ";\n    "])), ChangeBorderColor('color', color), duration);
}, function (_ref3) {
  var showEllipsis = _ref3.showEllipsis;
  return showEllipsis && "\n  overflow: hidden;\n  white-space: nowrap;\n  text-overflow: ellipsis;\n  width: 100%;\n    ";
});
var SubTitle = styled_components_browser_esm["b" /* default */].div(TaskMainWidget_styles_templateObject6 || (TaskMainWidget_styles_templateObject6 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 15px;\n  font-weight: normal;\n  font-stretch: normal;\n  font-style: normal;\n  letter-spacing: normal;\n  text-align: center;\n  color: ", ";\n"])), function (props) {
  return props.color ? function (props) {
    return props.color;
  } : "#ccc";
});
var Empty = styled_components_browser_esm["b" /* default */].div(TaskMainWidget_styles_templateObject7 || (TaskMainWidget_styles_templateObject7 = taggedTemplateLiteral_default()(["\n  height: ", ";\n"])), function (_ref4) {
  var height = _ref4.height;
  return height || "14px";
});
var Img = styled_components_browser_esm["b" /* default */].img(TaskMainWidget_styles_templateObject8 || (TaskMainWidget_styles_templateObject8 = taggedTemplateLiteral_default()([""])));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/styles.ts

var SourceAndEnv_styles_templateObject, SourceAndEnv_styles_templateObject2, SourceAndEnv_styles_templateObject3, SourceAndEnv_styles_templateObject4, SourceAndEnv_styles_templateObject5, SourceAndEnv_styles_templateObject6, SourceAndEnv_styles_templateObject7, SourceAndEnv_styles_templateObject8, SourceAndEnv_styles_templateObject9;


var styles_duration = "2s forwards";
var infinite = "2s infinite";
function styles_ChangeBorderColor(property, color) {
  var animation = "to {\n    ".concat(property, ": ").concat(color, ";\n  }");
  return Object(styled_components_browser_esm["c" /* keyframes */])(SourceAndEnv_styles_templateObject || (SourceAndEnv_styles_templateObject = taggedTemplateLiteral_default()(["\n    ", "\n  "])), animation);
}
function blinking(highlightColor) {
  var animation = "";
  if (highlightColor === "blue") {
    animation = "\n    0% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n    25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n    50% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n    75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n    100% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n    ";
  }
  if (highlightColor === "purple") {
    animation = "\n      0% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n      25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n      50% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n      75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n      100% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n      ";
  }
  return Object(styled_components_browser_esm["c" /* keyframes */])(SourceAndEnv_styles_templateObject2 || (SourceAndEnv_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    ", "\n  "])), animation);
}
var RectangleWrapper = styled_components_browser_esm["d" /* styled */].div(SourceAndEnv_styles_templateObject3 || (SourceAndEnv_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  position: relative;\n  width: 165px;\n  height: 165px;\n"])));
var ContentWrapper = styled_components_browser_esm["d" /* styled */].div(SourceAndEnv_styles_templateObject4 || (SourceAndEnv_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    height: 100%;\n    border-radius: inherit;\n    background-color: var(--white);\n    animation: ", " ", ";\n    display: flex;\n    flex-direction: column;\n    ", "\n    padding: ", ";\n    box-sizing: border-box;\n    align-items: center;\n    box-shadow:", ";\n    border: solid 4px\n      ", ";\n  "])), function (_ref) {
  var backgrounColor = _ref.backgrounColor;
  return styles_ChangeBorderColor('background-color', backgrounColor);
}, styles_duration, function (_ref2) {
  var centerTitle = _ref2.centerTitle;
  return centerTitle ? "\n    justify-content: space-between;\n    " : "\n\n    justify-content: center;\n    ";
}, function (props) {
  return props.padding;
}, function (props) {
  return props.boxShadow;
}, function (props) {
  return props.innerBorderColor;
});
var Rectangle = styled_components_browser_esm["d" /* styled */].div(SourceAndEnv_styles_templateObject5 || (SourceAndEnv_styles_templateObject5 = taggedTemplateLiteral_default()(["\n  width: 165px;\n  height: 165px;\n  padding: 12px;\n  border-radius: 20px;\n  border: solid 5px var(--disabled);\n  ", ";\n  position: absolute;\n  z-index: 100;\n  box-sizing: border-box;\n  user-select: none;\n  ", "\n"])), function (_ref3) {
  var highlightColor = _ref3.highlightColor,
    outerBorderColor = _ref3.outerBorderColor;
  return highlightColor === "none" ? Object(styled_components_browser_esm["a" /* css */])(SourceAndEnv_styles_templateObject6 || (SourceAndEnv_styles_templateObject6 = taggedTemplateLiteral_default()([" animation: ", " ", ";"])), styles_ChangeBorderColor('border-color', outerBorderColor), styles_duration) : Object(styled_components_browser_esm["a" /* css */])(SourceAndEnv_styles_templateObject7 || (SourceAndEnv_styles_templateObject7 = taggedTemplateLiteral_default()([" animation: ", " ", ";\n            border-color: ", ";\n     "])), blinking(highlightColor), infinite, outerBorderColor);
}, function (_ref4) {
  var status = _ref4.status;
  return status && status !== StatusEnum.disabled && "\n    cursor: pointer;\n  ";
});
var InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div(SourceAndEnv_styles_templateObject8 || (SourceAndEnv_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    z-index: 100;\n    top: -26px;\n    right: -10px;\n    cursor: auto;\n"])));
var SubTitleWrapper = styled_components_browser_esm["d" /* styled */].div(SourceAndEnv_styles_templateObject9 || (SourceAndEnv_styles_templateObject9 = taggedTemplateLiteral_default()(["\n  height: 28px;\n  display: flex;\n  align-items: end;\n"])));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/services.ts

var services_getIndicatorColors = function getIndicatorColors(status, name) {
  var primaryColor = name === "target" ? "var(--sky-blue)" : name === "test_data_store" ? "#8146f0" : "var(--lovelyPurple)";
  var secondaryColor = name === "target" ? "var(--sky-blue)" : name === "test_data_store" ? "#2c75f2" : "var(--lovelyPurple)";
  var borderColor = "linear-gradient(to right,".concat(primaryColor, ",").concat(secondaryColor, ")");
  var dotsColor = primaryColor;
  var dotsBackground = "linear-gradient(to right, #FFFFFF, #FFFFFF)";
  if (status === StatusEnum.completed) {
    dotsBackground = "linear-gradient(to right,".concat(primaryColor, ",").concat(secondaryColor, ")");
  }
  return {
    borderColor: borderColor,
    dotsColor: dotsColor,
    dotsBackground: dotsBackground
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/MenuIconSVG.tsx


function MenuIconSVG(props) {
  var color = props.color;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("svg", {
    xmlns: "http://www.w3.org/2000/svg",
    width: "16",
    height: "4",
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])("path", {
      "fill-rule": "evenodd",
      fill: color,
      d: "M13.395 3.733a1.776 1.776 0 1 1 0-3.551 1.776 1.776 0 0 1 0 3.551zm-5.683 0a1.775 1.775 0 1 1 .001-3.55 1.775 1.775 0 0 1-.001 3.55zm-5.682 0a1.775 1.775 0 1 1 0-3.55 1.775 1.775 0 0 1 0 3.55z"
    })
  });
}
/* harmony default export */ var Indicator_MenuIconSVG = (/*#__PURE__*/Object(react["memo"])(MenuIconSVG));
// CONCATENATED MODULE: ./src/images/vIcon.svg
/* harmony default export */ var vIcon = ("js/dist/7b73c75b47d3e4e80d5d20e37bbd2331.svg");
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/styles.ts

var Indicator_styles_templateObject, Indicator_styles_templateObject2, Indicator_styles_templateObject3;

var CicleMenuIconWrapper = styled_components_browser_esm["d" /* styled */].div(Indicator_styles_templateObject || (Indicator_styles_templateObject = taggedTemplateLiteral_default()(["\n  width: 30px;\n  height: 30px;\n  display: flex;\n  align-items: center;\n  justify-content: center;\n  box-sizing: border-box;\n  position: absolute;\n  margin-left: auto;\n  margin-right: auto;\n  right: 0;\n  left: 0;\n  bottom: -21px;\n"])));
var MenuIconGradiant = styled_components_browser_esm["d" /* styled */].div(Indicator_styles_templateObject2 || (Indicator_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    background-image: ", ";\n    border-radius: 50%;\n    width: 30px;\n    height: 30px;\n"])), function (props) {
  return props.borderBackgroundImage ? props.borderBackgroundImage : "none";
});
var IndicatorContent = styled_components_browser_esm["d" /* styled */].div(Indicator_styles_templateObject3 || (Indicator_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  position: absolute;\n  width: 24px;\n  height: 24px;\n  background-image: ", ";\n  display: flex;\n  justify-content: center;\n  align-items: center;\n  border-radius: 50%;\n  top: 3px;\n  left: 3px;\n"])), function (props) {
  return props.dotsBackground ? props.dotsBackground : "none";
});
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/index.tsx










function Indicator(props) {
  var status = props.status,
    name = props.name;
  var _useState = Object(react["useState"])(function () {
      return services_getIndicatorColors(status, name);
    }),
    _useState2 = slicedToArray_default()(_useState, 2),
    colors = _useState2[0],
    setColors = _useState2[1];
  Object(react["useEffect"])(function () {
    setColors(function () {
      return services_getIndicatorColors(status, name);
    });
  }, [status, name]);
  return status !== StatusEnum.disabled ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(CicleMenuIconWrapper, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(MenuIconGradiant, {
      borderBackgroundImage: colors.borderColor
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(IndicatorContent, {
      dotsBackground: colors.dotsBackground,
      children: [status === StatusEnum.enabled || status === StatusEnum.partial || status === StatusEnum.blink ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Indicator_MenuIconSVG, {
        color: colors.dotsColor
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {}), status === StatusEnum.completed ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Img, {
        src: vIcon,
        alt: "v icon"
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})]
    })]
  }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {});
}
/* harmony default export */ var TaskMainWidget_Indicator = (/*#__PURE__*/Object(react["memo"])(Indicator));
// CONCATENATED MODULE: ./src/components/Tooltip/styles.ts

var Tooltip_styles_templateObject, Tooltip_styles_templateObject2, Tooltip_styles_templateObject3;

var styles_TooltipContainer = styled_components_browser_esm["b" /* default */].div(Tooltip_styles_templateObject || (Tooltip_styles_templateObject = taggedTemplateLiteral_default()(["\n  position: relative;\n  display: inline-block;\n\n  .tooltip-text {\n    display: none;\n    position: absolute;\n    ", "\n    padding: 10px;\n    font-family: Roboto;\n    font-size: 17px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    z-index: 101;\n    width: max-content;\n    max-width: 250px;\n    max-height: 400px;\n    top: -11px;\n    ", "\n\n    &:before {\n      content: '';\n      position: absolute;\n      ", "\n      border-style: solid;\n      border-width: 5px;\n      z-index: 102;\n\n      ", "\n    }\n\n    &:after {\n      content: '';\n      position: absolute;\n      ", "\n      border-style: solid;\n      border-width: 6px;\n      z-index: -1;\n\n      ", "\n    }\n  }\n  &:hover .tooltip-text {\n    display: block;\n  }\n"])), function (_ref) {
  var hideTriangle = _ref.hideTriangle;
  return !hideTriangle ? "border-radius:3px;\n      border: solid 1px #ccc;\n      background-color: #fff;\n      " : "\n       border-radius:3px;\n       border: solid 1px #ccc;\n       background: #fff;\n      ";
}, function (_ref2) {
  var position = _ref2.position;
  return position === 'left' ? "\n      right: calc(100% + 10px);\n    " : "\n      left: calc(100% + 10px);\n    ";
}, function (_ref3) {
  var hideTriangle = _ref3.hideTriangle;
  return !hideTriangle ? "display:block;" : "display:none;";
}, function (_ref4) {
  var position = _ref4.position;
  return position === 'left' ? "\n        right: -10px;\n        top: 50%;\n        transform: translateY(-50%);\n        border-color: transparent transparent transparent #FFFFFF;\n      " : "\n        left: -10px;\n        top: 50%;\n        transform: translateY(-50%);\n        border-color: transparent  #FFFFFF transparent transparent;\n      ";
}, function (_ref5) {
  var hideTriangle = _ref5.hideTriangle;
  return !hideTriangle ? "display:block;" : "display:none;";
}, function (_ref6) {
  var position = _ref6.position;
  return position === 'left' ? "\n        right: -12px;\n        top: 50%;\n        transform: translateY(-50%);\n        border-color: transparent transparent transparent #ccc;;\n      " : "\n        left: -12px;\n        top: 50%;\n        transform: translateY(-50%);\n        border-color: transparent #ccc transparent transparent;\n      ";
});
var UL = styled_components_browser_esm["b" /* default */].ul(Tooltip_styles_templateObject2 || (Tooltip_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  margin: 0;\n  padding: 0 0 0 12px;\n"])));
var LI = styled_components_browser_esm["b" /* default */].li(Tooltip_styles_templateObject3 || (Tooltip_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  padding:0 !important;\n  padding-bottom: 4px !important;\n  background-color: transparent !important;\n"])));
// CONCATENATED MODULE: ./src/components/Tooltip/index.tsx
// Tooltip.tsx




var Tooltip_Tooltip = function Tooltip(_ref) {
  var children = _ref.children,
    position = _ref.position,
    textArray = _ref.textArray,
    hideTriangle = _ref.hideTriangle;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TooltipContainer, {
    position: position,
    hideTriangle: hideTriangle,
    children: [children, /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
      className: "tooltip-text",
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(UL, {
          children: textArray.map(function (text) {
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(LI, {
              children: text
            });
          })
        })
      })
    })]
  });
};
/* harmony default export */ var components_Tooltip = (Tooltip_Tooltip);
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/services.tsx

var services_getColors = function getColors(status, isTarget, isSelected, isConnectionColored) {
  var primaryColor = isTarget ? 'var(--sky-blue)' : 'var(--lovelyPurple)';
  var boxShadow = 'none';
  var subTitleColor = 'var(--disabled)';
  var titleColor = 'var(--disabled)';
  var backgroundColor = 'var(--white)';
  var innerBorderColor = 'transparent';
  var outerBorderColor = 'var(--disabled)';
  var arrowTriangleColor = 'var(--disabled)';
  var highlightColor = 'none';
  if (status !== StatusEnum.disabled) {
    boxShadow = '0px 10px 21px 0 rgba(51, 51, 51, 0.27)';
  }
  if (status === StatusEnum.partial) {
    outerBorderColor = primaryColor;
  }
  if (status === StatusEnum.blink) {
    outerBorderColor = primaryColor;
    highlightColor = isTarget ? 'blue' : 'purple';
  }
  if (status !== StatusEnum.disabled && isSelected) {
    subTitleColor = isTarget ? 'var(--milk-White)' : 'var(--white-opacity)';
    backgroundColor = primaryColor;
    titleColor = 'var(--white)';
    outerBorderColor = primaryColor;
  }
  if (status === StatusEnum.completed && !isSelected) {
    subTitleColor = 'var(--dusty-grey)';
    titleColor = 'var(--black-title)';
    innerBorderColor = primaryColor;
    outerBorderColor = primaryColor;
  }
  if (isConnectionColored) {
    arrowTriangleColor = primaryColor;
  }
  return {
    innerBorderColor: innerBorderColor,
    outerBorderColor: outerBorderColor,
    backgroundColor: backgroundColor,
    titleColor: titleColor,
    subTitleColor: subTitleColor,
    boxShadow: boxShadow,
    arrowTriangleColor: arrowTriangleColor,
    highlightColor: highlightColor
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/index.tsx











function SourceAndEnv(props) {
  var title = props.title,
    status = props.status,
    onClick = props.onClick,
    isSelected = props.isSelected,
    info = props.info,
    id = props.id,
    isTarget = props.isTarget,
    subTitle = props.subTitle,
    placeHolder = props.placeHolder,
    subTitlePlaceHolder = props.subTitlePlaceHolder,
    isConnectionColored = props.isConnectionColored;
  var _useState = Object(react["useState"])(function () {
      return services_getColors(status, isTarget, isSelected, isConnectionColored);
    }),
    _useState2 = slicedToArray_default()(_useState, 2),
    colors = _useState2[0],
    setColors = _useState2[1];
  Object(react["useEffect"])(function () {
    setColors(function () {
      return services_getColors(status, isTarget, isSelected, isConnectionColored);
    });
  }, [status, isSelected, isTarget, isConnectionColored]);
  var handleClick = Object(react["useCallback"])(function (event) {
    if (status === StatusEnum.disabled) return;
    onClick(event);
  }, [status, onClick]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(RectangleWrapper, {
    onClick: handleClick,
    id: id,
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Rectangle, {
      outerBorderColor: colors.outerBorderColor,
      status: status,
      highlightColor: colors.highlightColor,
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(InfoIconWrapper, {
        children: status !== StatusEnum.disabled && info && info.length ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Tooltip, {
          position: "right",
          textArray: info,
          hideTriangle: true,
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Img, {
            src: info_icon,
            alt: "info icon"
          })
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ContentWrapper, {
        padding: "12px",
        backgrounColor: colors.backgroundColor,
        innerBorderColor: colors.innerBorderColor,
        boxShadow: colors.boxShadow,
        centerTitle: !!title,
        children: [title ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Empty, {
          height: "28px"
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_styles_Title, {
          title: title ? title : placeHolder,
          showEllipsis: !!title && subTitle !== ' ',
          color: colors.titleColor,
          children: title ? title : placeHolder
        }), title ? /*#__PURE__*/Object(jsx_runtime["jsx"])(SubTitleWrapper, {
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(SubTitle, {
            color: colors.subTitleColor,
            children: title ? '' : ''
          })
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_Indicator, {
        status: status,
        name: id
      })]
    }), isConnectionColored !== undefined ? /*#__PURE__*/Object(jsx_runtime["jsx"])(ArrowTriangle, {
      left: "".concat(isTarget ? '-84%;' : '103%'),
      color: colors.arrowTriangleColor
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})]
  });
}
var MemoizedDataSource = /*#__PURE__*/Object(react["memo"])(SourceAndEnv);
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/services.ts

var services_getTestDataStoreColors = function getTestDataStoreColors(status, isSelected) {
  var boxShadow = "none";
  var subTitleColor = "var(--disabled)";
  var titleColor = "var(--disabled)";
  var backgroundColor = "var(--white)";
  var outerBorderBackgroundImage = "linear-gradient(to right, var(--disabled), var(--disabled))";
  var innerBorderBackgroundImage = "linear-gradient(to right,var(--white), var(--white))";
  if (status === StatusEnum.enabled) {
    subTitleColor = "var(--dusty-grey)";
  }
  if (status !== StatusEnum.disabled) {
    boxShadow = "0px 10px 21px 0 rgba(51, 51, 51, 0.27)";
  }
  if (status === StatusEnum.partial) {
    outerBorderBackgroundImage = "linear-gradient(to right, #8146f0, #2c75f2)";
  }
  if (isSelected) {
    backgroundColor = "linear-gradient(to right, #8146f0, #2c75f2)";
    titleColor = "var(--white)";
    subTitleColor = "var(--white)";
    outerBorderBackgroundImage = "linear-gradient(to right, #8146f0, #2c75f2)";
    innerBorderBackgroundImage = "linear-gradient(to right, #8146f0, #2c75f2)";
  }
  if (status === StatusEnum.completed && !isSelected) {
    backgroundColor = "var(--white)";
    titleColor = "var(--black-title)";
    subTitleColor = "var(--black-title)";
    outerBorderBackgroundImage = "linear-gradient(to right, #8146f0, #2c75f2)";
    innerBorderBackgroundImage = "linear-gradient(to right, #8146f0, #2c75f2)";
  }
  return {
    boxShadow: boxShadow,
    backgroundColor: backgroundColor,
    titleColor: titleColor,
    subTitleColor: subTitleColor,
    outerBorderBackgroundImage: outerBorderBackgroundImage,
    innerBorderBackgroundImage: innerBorderBackgroundImage
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/TestDataStoreIconSVG.tsx


function TestDataStoreIconSVG(props) {
  var color = props.color;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("svg", {
    xmlns: "http://www.w3.org/2000/svg",
    width: "101",
    height: "104",
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])("path", {
      "fill-rule": "evenodd",
      fill: color,
      d: "M98.694 103.996c-1.268 0-2.295-.978-2.295-2.191 0-1.209 1.027-2.189 2.295-2.189 1.269 0 2.295.98 2.295 2.189 0 1.213-1.026 2.191-2.295 2.191zm-9.18 0c-1.269 0-2.296-.978-2.296-2.191 0-1.209 1.027-2.189 2.296-2.189 1.268 0 2.295.98 2.295 2.189 0 1.213-1.027 2.191-2.295 2.191zM51.642 80.994v19.716h28.69c.635 0 1.148.49 1.148 1.095s-.513 1.096-1.148 1.096H20.655c-.633 0-1.147-.491-1.147-1.096 0-.605.514-1.095 1.147-1.095h28.692V80.994c-21.972-.209-39.02-6.378-39.02-14.223V14.219c0-7.981 17.642-14.233 40.167-14.233 22.524 0 40.168 6.252 40.168 14.233v52.552c0 7.845-17.049 14.014-39.02 14.223zM50.494 2.175c-22.654 0-37.871 6.227-37.871 12.044S27.84 26.262 50.494 26.262c22.654 0 37.871-6.226 37.871-12.043 0-5.817-15.217-12.044-37.871-12.044zm37.871 16.876c-5.469 5.533-20.157 9.401-37.871 9.401-17.715 0-32.403-3.868-37.871-9.402v12.686c0 5.817 15.217 12.043 37.871 12.043 22.654 0 37.871-6.226 37.871-12.043V19.051zm0 17.517c-5.469 5.534-20.157 9.401-37.871 9.401-17.715 0-32.403-3.867-37.871-9.401v12.685c0 5.817 15.217 12.044 37.871 12.044 22.654 0 37.871-6.227 37.871-12.044V36.568zm0 17.517c-5.469 5.534-20.157 9.401-37.871 9.401-17.715 0-32.403-3.868-37.871-9.402v12.687c0 5.816 15.217 12.044 37.871 12.044 22.654 0 37.871-6.228 37.871-12.044V54.085zm-74.595 47.72c0 1.213-1.026 2.191-2.295 2.191-1.268 0-2.296-.978-2.296-2.191 0-1.209 1.028-2.189 2.296-2.189 1.269 0 2.295.98 2.295 2.189zm-11.476 2.191c-1.269 0-2.295-.978-2.295-2.191 0-1.209 1.026-2.189 2.295-2.189 1.268 0 2.295.98 2.295 2.189 0 1.213-1.027 2.191-2.295 2.191z"
    })
  });
}
/* harmony default export */ var TestDataStore_TestDataStoreIconSVG = (/*#__PURE__*/Object(react["memo"])(TestDataStoreIconSVG));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/TestDataStoreTextSVG.tsx


function TestDataStoreTextSVG(props) {
  var color = props.color;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("svg", {
    xmlns: "http://www.w3.org/2000/svg",
    width: "136",
    height: "35",
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])("path", {
      "fill-rule": "evenodd",
      fill: color,
      d: "m134.273 8.266.745.931-5.563 4.327-6.913-8.627 5.502-4.281.746.93-4.349 3.383 2.228 2.777 3.795-2.949.745.928-3.794 2.953 2.448 3.056 4.41-3.428zm-12.071 4.445 4.762 2.535.05.075-1.33.839-4.474-2.456-2.206 1.395 2.435 3.743-1.243.786-6.022-9.252 3.108-1.965c1.059-.667 2.027-.944 2.905-.83.879.114 1.613.626 2.203 1.532.374.576.544 1.179.506 1.806-.037.627-.269 1.225-.694 1.792zm-1.059-2.815c-.373-.573-.832-.903-1.377-.99-.545-.087-1.145.074-1.802.484l-1.896 1.197 2.288 3.514 1.902-1.201c.615-.389 1.001-.854 1.161-1.4.158-.542.068-1.078-.276-1.604zm-6.465 6.972c.488.966.749 1.886.784 2.766.036.881-.154 1.659-.566 2.333-.411.676-1.02 1.21-1.824 1.606-.786.385-1.58.538-2.38.457-.801-.083-1.549-.394-2.24-.932-.694-.539-1.28-1.271-1.759-2.194l-.359-.709c-.478-.947-.734-1.864-.769-2.754-.033-.889.159-1.675.574-2.359.415-.686 1.017-1.222 1.802-1.608.801-.393 1.601-.548 2.404-.469.804.082 1.552.402 2.246.964.697.559 1.287 1.32 1.772 2.278l.315.621zm-1.261.74-.371-.73c-.59-1.166-1.28-1.943-2.073-2.335-.789-.389-1.613-.375-2.468.045-.831.408-1.329 1.043-1.49 1.901-.163.859.031 1.845.577 2.96l.367.723c.571 1.131 1.262 1.899 2.071 2.31.811.408 1.639.405 2.484-.01.85-.418 1.354-1.048 1.512-1.886.157-.84-.046-1.833-.609-2.978zm-11.498 10.54-3.49-9.185-3.35 1.238-.422-1.111 8.08-2.981.422 1.111-3.36 1.239 3.492 9.182-1.372.507zm-10.172-6.581c-.513-.259-1.147-.293-1.903-.094-.702.181-1.207.479-1.518.885-.312.407-.393.88-.248 1.421.117.435.402.752.855.955.454.203 1.163.311 2.125.325.963.015 1.737.107 2.324.273.586.167 1.056.42 1.408.754.351.335.6.773.744 1.312.232.863.076 1.639-.467 2.337-.54.696-1.383 1.192-2.522 1.489a5.321 5.321 0 0 1-2.189.122c-.715-.113-1.315-.367-1.794-.761a2.908 2.908 0 0 1-.966-1.522l1.429-.372c.172.643.55 1.087 1.131 1.333.581.248 1.275.264 2.079.054.752-.195 1.287-.495 1.606-.902.318-.406.408-.869.268-1.39-.141-.52-.433-.874-.88-1.062-.447-.189-1.194-.294-2.241-.318-1.313-.027-2.314-.221-3.005-.579-.691-.358-1.137-.906-1.335-1.647-.225-.837-.071-1.617.46-2.341.533-.725 1.341-1.227 2.427-1.512.742-.193 1.44-.225 2.096-.093.655.13 1.21.403 1.661.814.452.414.758.915.916 1.504l-1.43.373c-.172-.645-.515-1.097-1.031-1.358zm-13.315 9.365-4.646.507-.724 2.973-1.507.164 3.009-11.387 1.28-.139 5.466 10.464-1.499.163-1.379-2.745zm-3.03-6.033-1.307 5.314 3.77-.409-2.463-4.905zm-8.832 9.778-1.463.024-.162-9.804-3.577.055-.02-1.185 8.627-.137.021 1.185-3.587.058.161 9.804zm-8.345-2.996-4.66-.358-1.273 2.788-1.513-.116 5.116-10.632 1.283.099 3.387 11.284-1.505-.116-.835-2.949zm-1.83-6.486-2.293 4.981 3.781.292-1.488-5.273zm-7.096 3.371c-.191 1.022-.56 1.877-1.104 2.572a4.308 4.308 0 0 1-2.044 1.454c-.818.274-1.72.327-2.706.154l-3.036-.555 2.04-10.804 3.096.565c.954.175 1.758.538 2.413 1.087a4.284 4.284 0 0 1 1.364 2.086c.255.84.29 1.757.108 2.752l-.131.689zm-1.642-3.949c-.457-.786-1.222-1.286-2.295-1.493l-1.687-.309-1.6 8.477 1.523.278c1.114.204 2.046.021 2.795-.549.75-.568 1.243-1.486 1.482-2.748l.118-.632c.233-1.228.12-2.237-.336-3.024zm-14.953-4.772L29.47 29.12l-1.385-.468 3.235-9.274-3.385-1.146.391-1.12 8.16 2.761-.39 1.121-3.392-1.149zm-8.304-.954c.282-.605.308-1.17.08-1.693-.229-.523-.698-.945-1.409-1.266-.659-.299-1.24-.388-1.742-.268-.503.118-.872.434-1.109.942-.191.408-.176.832.044 1.271.219.441.695.968 1.429 1.584.734.613 1.272 1.17 1.617 1.668.346.495.546.982.601 1.463.055.477-.036.971-.272 1.479-.377.809-1 1.314-1.868 1.51-.867.196-1.837.053-2.91-.432a5.23 5.23 0 0 1-1.766-1.276c-.481-.536-.779-1.106-.892-1.712a2.85 2.85 0 0 1 .234-1.78l1.344.608c-.281.605-.277 1.183.012 1.738.29.555.812 1.002 1.57 1.344.706.321 1.312.424 1.82.311.508-.115.877-.416 1.105-.906.228-.489.23-.946.006-1.371-.223-.425-.731-.974-1.524-1.649-.995-.843-1.645-1.62-1.946-2.33a2.476 2.476 0 0 1 .034-2.106c.366-.789.988-1.293 1.867-1.52.878-.226 1.828-.106 2.849.355.695.315 1.255.728 1.677 1.239.422.514.674 1.07.755 1.672a3.039 3.039 0 0 1-.263 1.733l-1.343-.608zm-11.449-8.049-1.808 3.06 4.18 2.395-.605 1.026-4.179-2.396-1.989 3.366 4.853 2.783-.605 1.024-6.125-3.512 5.613-9.499 6.058 3.474-.605 1.025-4.788-2.746zM6.984 6.976l-5.812 7.961-1.187-.844 5.811-7.96-2.905-2.057.702-.964 7.005 4.962-.703.962-2.911-2.06z"
    })
  });
}
/* harmony default export */ var TestDataStore_TestDataStoreTextSVG = (/*#__PURE__*/Object(react["memo"])(TestDataStoreTextSVG));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/styles.ts

var TestDataStore_styles_templateObject, TestDataStore_styles_templateObject2, TestDataStore_styles_templateObject3, TestDataStore_styles_templateObject4, TestDataStore_styles_templateObject5, TestDataStore_styles_templateObject6, TestDataStore_styles_templateObject7, TestDataStore_styles_templateObject8, TestDataStore_styles_templateObject9, TestDataStore_styles_templateObject10, TestDataStore_styles_templateObject11;


var TestDataStore_styles_duration = "1s forwards";
function TestDataStore_styles_ChangeBorderColor(property, color) {
  var animation = "to {\n    ".concat(property, ": ").concat(color, ";\n  }");
  return Object(styled_components_browser_esm["c" /* keyframes */])(TestDataStore_styles_templateObject || (TestDataStore_styles_templateObject = taggedTemplateLiteral_default()(["\n    ", "\n  "])), animation);
}
var CicleContentWrapper = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject2 || (TestDataStore_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 216px;\n    height: 216px;\n    border-radius: inherit;\n    display: flex;\n    flex-direction: column;\n    justify-content: space-between;\n    background:var(--white);\n    ", "\n    padding: ", ";\n    box-sizing: border-box;\n    align-items: center;\n    position: absolute;\n      top: 16px;\n      left: 16px;\n  "])), function (_ref) {
  var backgrounColor = _ref.backgrounColor;
  return backgrounColor && Object(styled_components_browser_esm["a" /* css */])(TestDataStore_styles_templateObject3 || (TestDataStore_styles_templateObject3 = taggedTemplateLiteral_default()(["\n      animation: ", " ", ";\n    "])), TestDataStore_styles_ChangeBorderColor('background', backgrounColor), TestDataStore_styles_duration);
}, function (props) {
  return props.padding;
});
var CircleGradiantWrapper = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject4 || (TestDataStore_styles_templateObject4 = taggedTemplateLiteral_default()(["\nposition: absolute;\nz-index: 2;\n"])));
var CircleGradiant = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject5 || (TestDataStore_styles_templateObject5 = taggedTemplateLiteral_default()(["\nbackground: var(--disabled);\n", "\nborder-radius: 50%;\nwidth: 258px;\nheight: 258px;\n"])), function (_ref2) {
  var borderBackgroundImage = _ref2.borderBackgroundImage;
  return borderBackgroundImage && Object(styled_components_browser_esm["a" /* css */])(TestDataStore_styles_templateObject6 || (TestDataStore_styles_templateObject6 = taggedTemplateLiteral_default()(["\n     animation: ", " ", ";\n  "])), TestDataStore_styles_ChangeBorderColor('background', borderBackgroundImage), TestDataStore_styles_duration);
});
var CircleContentGradiant = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject7 || (TestDataStore_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    background:transparent;\n    ", "\n    box-shadow:", ";\n    border-radius: 50%;\n    width: 224px;\n    height: 224px;\n"])), function (_ref3) {
  var borderBackgroundImage = _ref3.borderBackgroundImage;
  return borderBackgroundImage && Object(styled_components_browser_esm["a" /* css */])(TestDataStore_styles_templateObject8 || (TestDataStore_styles_templateObject8 = taggedTemplateLiteral_default()(["\n      animation: ", " ", ";\n    "])), TestDataStore_styles_ChangeBorderColor('background', borderBackgroundImage), TestDataStore_styles_duration);
}, function (props) {
  return props.boxShadow;
});
var Circle = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject9 || (TestDataStore_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    width: 248px;\n    height: 248px;\n  padding: 12px;\n  position: relative;\n  border-radius: 50%;\n  box-sizing: border-box;\n  background-color:#f2f2f2;\n  position: absolute;\n  top: 5px;\n    left: 5px;\n  ", "\n    box-shadow:", ";\n"])), function (_ref4) {
  var status = _ref4.status;
  return status && status !== StatusEnum.disabled && "\n    cursor: pointer;\n  ";
}, function (props) {
  return props.boxShadow;
});
var TestDataStoreWrapper = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject10 || (TestDataStore_styles_templateObject10 = taggedTemplateLiteral_default()(["\n position:relative;\n width:258px;\n height:258px\n"])));
var styles_InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div(TestDataStore_styles_templateObject11 || (TestDataStore_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    top: 10px;\n    right: 20px;\n    cursor: auto;\n    z-index: 102;\n"])));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/index.tsx













function TestDataStore(props) {
  var status = props.status,
    onClick = props.onClick,
    isSelected = props.isSelected,
    info = props.info;
  var _useState = Object(react["useState"])(function () {
      return services_getTestDataStoreColors(status, isSelected);
    }),
    _useState2 = slicedToArray_default()(_useState, 2),
    colors = _useState2[0],
    setColors = _useState2[1];
  Object(react["useEffect"])(function () {
    setColors(function () {
      return services_getTestDataStoreColors(status, isSelected);
    });
  }, [status, isSelected]);
  var handleClick = Object(react["useCallback"])(function (event) {
    if (status === StatusEnum.disabled) return;
    onClick(event);
  }, [status, onClick]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TestDataStoreWrapper, {
    onClick: handleClick,
    id: "test_data_store",
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_InfoIconWrapper, {
      children: info && info.length ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Tooltip, {
        position: "right",
        textArray: info,
        hideTriangle: true,
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Img, {
          src: info_icon,
          alt: "info icon"
        })
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(CircleGradiantWrapper, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CircleGradiant, {
        borderBackgroundImage: colors.outerBorderBackgroundImage
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Circle, {
        boxShadow: colors.boxShadow,
        status: status,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CircleContentGradiant, {
          boxShadow: colors.boxShadow,
          borderBackgroundImage: colors.innerBorderBackgroundImage
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(CicleContentWrapper, {
          padding: "15px",
          backgrounColor: colors.backgroundColor,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Empty, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(TestDataStore_TestDataStoreIconSVG, {
            color: colors.titleColor
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TestDataStore_TestDataStoreTextSVG, {
            color: colors.subTitleColor
          })]
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_Indicator, {
          status: status,
          name: "test_data_store"
        })]
      })]
    })]
  });
}
/* harmony default export */ var TaskMainWidget_TestDataStore = (TestDataStore);
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/styles.ts

var Filter_styles_templateObject, Filter_styles_templateObject2, Filter_styles_templateObject3, Filter_styles_templateObject4, Filter_styles_templateObject5, Filter_styles_templateObject6, Filter_styles_templateObject7, Filter_styles_templateObject8, Filter_styles_templateObject9;


var FilterWrapper = styled_components_browser_esm["d" /* styled */].div(Filter_styles_templateObject || (Filter_styles_templateObject = taggedTemplateLiteral_default()(["\ncursor: pointer;\n"])));
var EntityIconWrapper = Object(styled_components_browser_esm["d" /* styled */])(SubTitle)(Filter_styles_templateObject2 || (Filter_styles_templateObject2 = taggedTemplateLiteral_default()(["\n   position: absolute;\n   left: 0;\n   right: 0;\n   bottom: 25px;\n"])));
var FilterIconWrapper = styled_components_browser_esm["d" /* styled */].div(Filter_styles_templateObject3 || (Filter_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 31px;\n    height: 31px;\n    border: solid 2px ", ";\n    background-color:", ";\n    border-radius: 50%;\n    display: flex;\n    align-items: center;\n    justify-content: center;\n    box-sizing: border-box;\n    position: absolute;\n    padding-top: 3px;\n    right: 0;\n    left: 0;\n    bottom: 0;\n    top: 0;\n    margin: auto;\n    animation: ", " 2s infinite;\n"])), function (props) {
  return props.outerBorderColor;
}, function (props) {
  return props.backgroundColor;
}, function (props) {
  if (props.highlightColor === "blue") {
    return Object(styled_components_browser_esm["c" /* keyframes */])(Filter_styles_templateObject4 || (Filter_styles_templateObject4 = taggedTemplateLiteral_default()(["\n      0% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n      25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n      50% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n      75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n      100% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}\n      "])));
  }
  if (props.highlightColor === "purple") {
    return Object(styled_components_browser_esm["c" /* keyframes */])(Filter_styles_templateObject5 || (Filter_styles_templateObject5 = taggedTemplateLiteral_default()(["\n        0% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n        25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n        50% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n        75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}\n        100% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}\n        "])));
  }
  return "";
});
var SubsetTitle = Object(styled_components_browser_esm["d" /* styled */])(SubTitle)(Filter_styles_templateObject6 || (Filter_styles_templateObject6 = taggedTemplateLiteral_default()(["\n   position: absolute;\n   left: 0;\n   right: 0;\n   top: 28px;\n"])));
var styles_Img = styled_components_browser_esm["d" /* styled */].img(Filter_styles_templateObject7 || (Filter_styles_templateObject7 = taggedTemplateLiteral_default()(["\n\n"])));
var Filter_styles_InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div(Filter_styles_templateObject8 || (Filter_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    position: relative;\n"])));
var InfoIconContainer = styled_components_browser_esm["d" /* styled */].div(Filter_styles_templateObject9 || (Filter_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    top: -44px;\n    left: 48px;\n    cursor: auto;\n    z-index: 101;\n"])));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/FilterIconSVG.tsx


function FilterIcon(props) {
  var color = props.color;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("svg", {
    xmlns: "http://www.w3.org/2000/svg",
    width: "17",
    height: "17",
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])("path", {
      "fill-rule": "evenodd",
      fill: color,
      d: "M7.635 16.469a.662.662 0 0 1-.25-.049.661.661 0 0 1-.405-.609v-6.95L.578 1.079A.664.664 0 0 1 .49.377a.656.656 0 0 1 .593-.378h15.071c.254 0 .485.148.593.378a.664.664 0 0 1-.088.702l-6.403 7.782v4.974a.658.658 0 0 1-.191.465l-1.966 1.976a.655.655 0 0 1-.464.193zM2.475 1.317l5.666 6.887a.66.66 0 0 1 .15.419v5.597l.656-.659V8.623c0-.153-.847-.301.15-.419l5.665-6.887H2.475z"
    })
  });
}
/* harmony default export */ var FilterIconSVG = (/*#__PURE__*/Object(react["memo"])(FilterIcon));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/services.tsx

var services_getSubsetColors = function getSubsetColors(subsetStatus, isDataSourceSubSet, isSelected) {
  var primaryColor = isDataSourceSubSet ? 'var(--lovelyPurple)' : 'var(--sky-blue)';
  var primaryHighlightColor = isDataSourceSubSet ? 'purple' : 'blue';
  var color = 'var(--white)';
  var fontColor = 'var(--black-title)';
  var subsetBorderColor = primaryColor;
  var highlightColor = 'none';
  var backgroundColor = color;
  var iconColor = primaryColor;
  if ((subsetStatus === StatusEnum.enabled || subsetStatus === StatusEnum.partial) && !isSelected) {
    highlightColor = primaryHighlightColor;
  }
  if (subsetStatus !== StatusEnum.disabled && isSelected) {
    highlightColor = 'none';
    subsetBorderColor = primaryColor;
    backgroundColor = primaryColor;
    iconColor = color;
  }
  return {
    subsetBorderColor: subsetBorderColor,
    highlightColor: highlightColor,
    backgroundColor: backgroundColor,
    iconColor: iconColor,
    fontColor: fontColor
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/index.tsx












function Filter_Filter(props) {
  var subsetStatus = props.subsetStatus,
    isDataSourceSubSet = props.isDataSourceSubSet,
    onClick = props.onClick,
    info = props.info,
    isSelected = props.isSelected;
  var _useState = Object(react["useState"])(function () {
      return services_getSubsetColors(subsetStatus, isDataSourceSubSet, isSelected);
    }),
    _useState2 = slicedToArray_default()(_useState, 2),
    colors = _useState2[0],
    setColors = _useState2[1];
  Object(react["useEffect"])(function () {
    setColors(function () {
      return services_getSubsetColors(subsetStatus, isDataSourceSubSet, isSelected);
    });
  }, [subsetStatus, isDataSourceSubSet, isSelected]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
    children: subsetStatus !== StatusEnum.disabled ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Filter_styles_InfoIconWrapper, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(InfoIconContainer, {
          children: info && info.length ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Tooltip, {
            position: "right",
            textArray: info,
            hideTriangle: true,
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Img, {
              src: info_icon,
              alt: "info icon"
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FilterWrapper, {
        onClick: onClick,
        id: "".concat(isDataSourceSubSet ? 'source_data_subset' : 'target_data_subset'),
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(FilterIconWrapper, {
          outerBorderColor: colors.subsetBorderColor,
          highlightColor: colors.highlightColor,
          backgroundColor: colors.backgroundColor,
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(FilterIconSVG, {
            color: colors.iconColor
          })
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SubsetTitle, {
          color: colors.fontColor,
          children: "Subset"
        })]
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})
  });
}
/* harmony default export */ var TaskMainWidget_Filter = (/*#__PURE__*/Object(react["memo"])(Filter_Filter));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/DashedHr/styles.ts

var DashedHr_styles_templateObject, DashedHr_styles_templateObject2, DashedHr_styles_templateObject3, DashedHr_styles_templateObject4;

var dash = Object(styled_components_browser_esm["c" /* keyframes */])(DashedHr_styles_templateObject || (DashedHr_styles_templateObject = taggedTemplateLiteral_default()(["\n0% {\n  background-position: 0px 0px, 600px 116px, 0px 150px, 416px 0px;\n}\n100% {\n  background-position: 600px 0px, 0px 116px, 0px 0px, 416px 150px;\n}\n"])));
var DashedHrWrapper = styled_components_browser_esm["d" /* styled */].div(DashedHr_styles_templateObject2 || (DashedHr_styles_templateObject2 = taggedTemplateLiteral_default()(["\n  display: flex;\n  flex-direction: row;\n  height: 5px;\n"])));
var DashedHr = styled_components_browser_esm["d" /* styled */].div(DashedHr_styles_templateObject3 || (DashedHr_styles_templateObject3 = taggedTemplateLiteral_default()(["\n  background-image: linear-gradient(\n    90deg,\n    ", " 50%,\n    transparent 50%\n  );\n  background-size: 40px 10px, 40px 10px, 40px 10px, 40px 10px;\n  animation: ", ";\n  width: 100%;\n  height: 100%;\n  display:", "\n"])), function (props) {
  return props.lineColor;
}, function (props) {
  return props.animation ? Object(styled_components_browser_esm["a" /* css */])(DashedHr_styles_templateObject4 || (DashedHr_styles_templateObject4 = taggedTemplateLiteral_default()(["\n          ", " 10s infinite linear\n        "])), dash) : "none";
}, function (_ref) {
  var display = _ref.display;
  return display;
});
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/DashedHr/services.tsx
var getDashedHrColors = function getDashedHrColors(isDataSourceSubSet, isConnectionColored) {
  var animation = false;
  var primaryColor = 'var(--disabled)';
  var secondaryColor = 'var(--disabled)';
  var color = isDataSourceSubSet ? 'var(--lovelyPurple)' : 'var(--sky-blue)';
  var display = isConnectionColored === undefined ? 'none' : 'block';
  if (isConnectionColored) {
    primaryColor = color;
    secondaryColor = 'var(--lovelyPurple)';
    animation = true;
  }
  return {
    primaryColor: primaryColor,
    secondaryColor: secondaryColor,
    animation: animation,
    display: display
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/DashedHr/index.tsx





function Subset(props) {
  var isDataSourceSubSet = props.isDataSourceSubSet,
    isConnectionColored = props.isConnectionColored;
  var _useState = Object(react["useState"])(function () {
      return getDashedHrColors(isDataSourceSubSet, isConnectionColored);
    }),
    _useState2 = slicedToArray_default()(_useState, 2),
    colors = _useState2[0],
    setColors = _useState2[1];
  Object(react["useEffect"])(function () {
    setColors(function () {
      return getDashedHrColors(isDataSourceSubSet, isConnectionColored);
    });
  }, [isDataSourceSubSet, isConnectionColored]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(DashedHrWrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(DashedHr, {
      lineColor: colors.primaryColor,
      animation: colors.animation,
      display: colors.display
    })
  });
}
/* harmony default export */ var TaskMainWidget_DashedHr = (/*#__PURE__*/Object(react["memo"])(Subset));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Subset/styles.ts

var Subset_styles_templateObject;

var SubsetWrapper = styled_components_browser_esm["d" /* styled */].div(Subset_styles_templateObject || (Subset_styles_templateObject = taggedTemplateLiteral_default()(["\n    position: relative;\n    width: calc((100% - (165px + 165px + 264px + 28px))/2);\n    padding-left: 14px;\n"])));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Subset/index.tsx






function Subset_Subset(props) {
  var subsetStatus = props.subsetStatus,
    dataStatus = props.dataStatus,
    isDataSourceSubSet = props.isDataSourceSubSet,
    onClick = props.onClick,
    info = props.info,
    isSelected = props.isSelected,
    isConnectionColored = props.isConnectionColored,
    subsetType = props.subsetType;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(SubsetWrapper, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_Filter, {
      subsetStatus: subsetStatus,
      dataStatus: dataStatus,
      isSelected: isSelected,
      isDataSourceSubSet: isDataSourceSubSet,
      onClick: onClick,
      info: info,
      subsetType: subsetType
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_DashedHr, {
      isDataSourceSubSet: isDataSourceSubSet,
      isConnectionColored: isConnectionColored
    })]
  });
}
/* harmony default export */ var TaskMainWidget_Subset = (/*#__PURE__*/Object(react["memo"])(Subset_Subset));
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/index.tsx








var StatusEnum = /*#__PURE__*/function (StatusEnum) {
  StatusEnum["disabled"] = "disabled";
  StatusEnum["enabled"] = "enabled";
  StatusEnum["completed"] = "completed";
  StatusEnum["partial"] = "partial";
  StatusEnum["selected"] = "selected";
  StatusEnum["blink"] = "blink";
  return StatusEnum;
}({});
var SubsetPossition = /*#__PURE__*/function (SubsetPossition) {
  SubsetPossition["source"] = "source";
  SubsetPossition["target"] = "target";
  SubsetPossition["undefined"] = "";
  return SubsetPossition;
}({});
function TaskMainWidget(props) {
  var onClick = props.onClick,
    data = props.data,
    source_environment_name = props.source_environment_name,
    environment_name = props.environment_name,
    targetInfo = props.targetInfo,
    sourceInfo = props.sourceInfo,
    subsetInfo = props.subsetInfo,
    datastoreInfo = props.datastoreInfo,
    selectedStep = props.selectedStep,
    subsetType = props.subsetType,
    sourceSubTitle = props.sourceSubTitle,
    targetSubTitle = props.targetSubTitle;
  //  const { onClick, source_environment_name, environment_name, targetInfo,subsetType,sourceSubTitle,targetSubTitle} = props;
  var handleClickOnStep = Object(react["useCallback"])(function (event) {
    onClick(event.currentTarget.id);
  }, [onClick]);

  //   const data ={
  //       dataSourceStatus: StatusEnum.partial,
  //       sourceSubsetStatus: StatusEnum.partial,
  //       targetSubsetStatus: StatusEnum.partial,
  //       testDataStoreStatus: StatusEnum.partial,
  //       targetStatus: StatusEnum.partial,
  //       isSourceConnectionEnabled:true,
  //       isTargetConnectionEnabled:true

  //   };
  // const selectedStep:string = "source";

  var _useMemo = Object(react["useMemo"])(function () {
      if (data.subsetPosition === SubsetPossition.source) {
        return {
          sourceSubsetStatus: data.subsetStatus,
          targetSubsetStatus: StatusEnum.disabled
        };
      }
      if (data.subsetPosition === SubsetPossition.target) {
        return {
          targetSubsetStatus: data.subsetStatus,
          sourceSubsetStatus: StatusEnum.disabled
        };
      }
      return {
        sourceSubsetStatus: StatusEnum.disabled,
        targetSubsetStatus: StatusEnum.disabled
      };
    }, [data.subsetStatus, data.subsetPosition]),
    targetSubsetStatus = _useMemo.targetSubsetStatus,
    sourceSubsetStatus = _useMemo.sourceSubsetStatus;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskMainWidget_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(MemoizedDataSource, {
      id: "source",
      isSelected: selectedStep === 'source',
      isConnectionColored: data.isSourceConnectionEnabled,
      title: source_environment_name,
      placeHolder: "Select Source Environment",
      subTitle: sourceSubTitle,
      subTitlePlaceHolder: "SOURCE ENVIRONMENT",
      info: sourceInfo,
      status: data.dataSourceStatus,
      onClick: handleClickOnStep,
      isTarget: false
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_Subset, {
      isSelected: selectedStep === 'source_data_subset',
      isConnectionColored: data.isSourceConnectionEnabled,
      isDataSourceSubSet: true,
      info: subsetInfo,
      subsetStatus: sourceSubsetStatus,
      dataStatus: data.dataSourceStatus,
      onClick: handleClickOnStep,
      subsetType: subsetType
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_TestDataStore, {
      isSelected: selectedStep === 'test_data_store',
      status: data.testDataStoreStatus,
      info: datastoreInfo,
      onClick: handleClickOnStep
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskMainWidget_Subset, {
      isSelected: selectedStep === 'target_data_subset',
      isConnectionColored: data.isTargetConnectionEnabled,
      isDataSourceSubSet: false,
      info: subsetInfo,
      subsetStatus: targetSubsetStatus,
      dataStatus: data.targetStatus,
      onClick: handleClickOnStep,
      subsetType: subsetType
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(MemoizedDataSource, {
      id: "target",
      isConnectionColored: data.isTargetConnectionEnabled,
      isSelected: selectedStep === 'target',
      title: environment_name,
      placeHolder: "Select Target Environment",
      subTitle: targetSubTitle,
      subTitlePlaceHolder: "TARGET ENVIRONMENT",
      status: data.targetStatus || '',
      onClick: handleClickOnStep,
      info: targetInfo,
      isTarget: true
    })]
  });
}
/* harmony default export */ var task_TaskMainWidget = (/*#__PURE__*/Object(react["memo"])(TaskMainWidget));
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Parameters/index.tsx



function Parameters_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Parameters_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Parameters_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Parameters_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }











function Parameters(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    resetField = _useContext.resetField,
    taskData = _useContext.taskData,
    statusesFuncMap = _useContext.statusesFuncMap,
    saveForm = _useContext.saveForm;
  var be_id = taskData.be_id,
    environment_id = taskData.environment_id,
    source_environment_name = taskData.source_environment_name,
    environment_name = taskData.environment_name,
    parameters = taskData.parameters,
    selection_method = taskData.selection_method,
    selection_param_value = taskData.selection_param_value,
    filterout_reserved = taskData.filterout_reserved,
    clone_ind = taskData.clone_ind,
    replace_sequences = taskData.replace_sequences,
    load_entity = taskData.load_entity,
    target_env = taskData.target_env,
    sync_mode = taskData.sync_mode,
    version_ind = taskData.version_ind,
    enable_param_lu_name = taskData.enable_param_lu_name;
  var _useState = Object(react["useState"])(0),
    _useState2 = slicedToArray_default()(_useState, 2),
    entitiesCount = _useState2[0],
    setEntitiesCount = _useState2[1];
  var _useState3 = Object(react["useState"])(false),
    _useState4 = slicedToArray_default()(_useState3, 2),
    loading = _useState4[0],
    setLoading = _useState4[1];
  var _useState5 = Object(react["useState"])(false),
    _useState6 = slicedToArray_default()(_useState5, 2),
    filterReserveError = _useState6[0],
    setFilterReserveError = _useState6[1];
  var _useState7 = Object(react["useState"])(null),
    _useState8 = slicedToArray_default()(_useState7, 2),
    parametersList = _useState8[0],
    setParametersList = _useState8[1];
  var _useState9 = Object(react["useState"])({
      group: {
        rules: [],
        operator: 'AND'
      }
    }),
    _useState10 = slicedToArray_default()(_useState9, 2),
    filter = _useState10[0],
    setFilter = _useState10[1];
  Object(react["useEffect"])(function () {
    var filter = undefined;
    try {
      filter = JSON.parse(parameters || '');
    } catch (err) {
      console.error(err.message);
    } finally {
      if (!filter) {
        filter = {
          group: {
            rules: [],
            operator: 'AND'
          }
        };
      } else if (!filter.group) {
        filter.group = {
          rules: [],
          operator: 'AND'
        };
      }
      setFilter(filter);
    }
  }, [parameters]);
  Object(react["useEffect"])(function () {
    // Create a variable to hold the timeout
    var timeoutId;
    // Set the timeout to delay the API call by 1.5 seconds
    timeoutId = setTimeout(function () {
      getEntitesCount();
    }, 1500);

    // Cleanup function to clear the timeout if useEffect is called again before 1.5 seconds
    return function () {
      return clearTimeout(timeoutId);
    };
  }, [selection_param_value, filterout_reserved, filter]);
  Object(react["useEffect"])(function () {
    var getData = /*#__PURE__*/function () {
      var _ref = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var sourceStatus, env_name, data, result;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              if (!(!be_id || !source_environment_name && !environment_name)) {
                _context.next = 2;
                break;
              }
              return _context.abrupt("return");
            case 2:
              sourceStatus = statusesFuncMap['dataSourceStatus'](taskData);
              env_name = (sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name) || '';
              _context.next = 6;
              return apis_task.getParameters(be_id, env_name);
            case 6:
              data = _context.sent;
              result = [];
              Object.keys(data).forEach(function (key) {
                var value = data[key];
                result.push({
                  label: enable_param_lu_name ? key : key.split('.')[1],
                  value: key,
                  param_name: key,
                  name: value.PARAM_NAME,
                  table: value.LU_PARAMS_TABLE_NAME,
                  param_type: value.PARAM_TYPE,
                  COMBO_INDICATOR: value.COMBO_INDICATOR === 'true',
                  valid_values: Array.isArray(value['VALID_VALUES']) ? value['VALID_VALUES'].map(function (it) {
                    return {
                      label: it,
                      value: it
                    };
                  }) : value['VALID_VALUES'],
                  min_value: value.PARAM_TYPE === 'REAL' || value.PARAM_TYPE === 'INTEGER' || value.PARAM_TYPE === 'NUMBER' ? parseFloat(value['MIN_VALUE']) : 0,
                  max_value: value.PARAM_TYPE === 'REAL' || value.PARAM_TYPE === 'INTEGER' || value.PARAM_TYPE === 'NUMBER' ? parseFloat(value['MAX_VALUE']) : 0
                });
              });
              setParametersList(result);
            case 10:
            case "end":
              return _context.stop();
          }
        }, _callee);
      }));
      return function getData() {
        return _ref.apply(this, arguments);
      };
    }();
    getData();
    if (filterout_reserved && !environment_id) {
      saveForm({
        filterout_reserved: 'OTHERS'
      });
      setFilterReserveError(true);
    }
  }, []);
  var parametersDataChange = Object(react["useCallback"])(function () {
    setFilter(Parameters_objectSpread({}, filter));
    var selection_param_value = getSelectionParamValue(filter, parametersList);
    saveForm({
      parameters: JSON.stringify(filter),
      selection_param_value: selection_param_value
    });
  }, [filter, saveForm, parametersList]);
  var parametersRandomChange = Object(react["useCallback"])(function (value) {
    saveForm({
      selection_method: value ? 'PR' : 'P'
    });
  }, [saveForm]);

  // const filterOutReservedChange = useCallback(
  //     (value: boolean | undefined) => {
  //         if (!environment_id && value) {
  //             setFilterReserveError(true);
  //             return;
  //         }
  //         saveForm({
  //             filterout_reserved: value,
  //         });
  //     },
  //     [saveForm, environment_id]
  // );

  var getEntitesCount = Object(react["useCallback"])(function () {
    if (!selection_param_value || selection_param_value === '()') {
      // setEntitiesCount(0);
      return;
    }
    var getData = /*#__PURE__*/function () {
      var _ref2 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
        var local_filterout_reserved, sourceStatus, body, data;
        return regenerator_default.a.wrap(function _callee2$(_context2) {
          while (1) switch (_context2.prev = _context2.next) {
            case 0:
              if (!(!be_id || !source_environment_name && !environment_name)) {
                _context2.next = 2;
                break;
              }
              return _context2.abrupt("return");
            case 2:
              setLoading(true);
              local_filterout_reserved = filterout_reserved;
              if ((clone_ind || replace_sequences) && load_entity || target_env === 'ai_training' || !environment_id || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL') {
                local_filterout_reserved = 'NA';
              }
              sourceStatus = statusesFuncMap['dataSourceStatus'](taskData);
              body = {
                where: selection_param_value,
                tar_env_name: environment_name,
                src_env_name: sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name,
                queryJson: JSON.stringify(filter),
                filterout_reserved: local_filterout_reserved || 'NA'
              };
              console.log(body);
              _context2.prev = 8;
              _context2.next = 11;
              return apis_task.getEntitiesCount(be_id, (sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name) || '', body);
            case 11:
              data = _context2.sent;
              setEntitiesCount(data);
              _context2.next = 18;
              break;
            case 15:
              _context2.prev = 15;
              _context2.t0 = _context2["catch"](8);
              setEntitiesCount(0);
            case 18:
              _context2.prev = 18;
              setLoading(false);
              return _context2.finish(18);
            case 21:
            case "end":
              return _context2.stop();
          }
        }, _callee2, null, [[8, 15, 18, 21]]);
      }));
      return function getData() {
        return _ref2.apply(this, arguments);
      };
    }();
    getData();
  }, [be_id, selection_param_value, source_environment_name, environment_name, filterout_reserved, setLoading, filter, clone_ind, replace_sequences, load_entity, target_env, sync_mode, version_ind, statusesFuncMap]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Parameters_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(Parameters_styles_RightSide, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(RefreshParameters, {
        children: [loading ? /*#__PURE__*/Object(jsx_runtime["jsx"])(AnimationIcon, {
          src: refresh,
          onClick: getEntitesCount
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(Parameters_styles_Icon, {
          src: refresh,
          onClick: getEntitesCount
        }), "Number of entities matched = ", entitiesCount]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(DateFormatNote, {
        children: "For date parameters, use YYYY-MM-DD format"
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_QueryBuilder, {
        parent: [],
        onChange: parametersDataChange,
        index: '0',
        parameters: parametersList,
        group: filter.group
      })]
    })
  });
}
/* harmony default export */ var DataSubset_Parameters = (Parameters);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Tables/styles.ts

var Tables_styles_templateObject, Tables_styles_templateObject2, Tables_styles_templateObject3, Tables_styles_templateObject4, Tables_styles_templateObject5, Tables_styles_templateObject6, Tables_styles_templateObject7, Tables_styles_templateObject8, Tables_styles_templateObject9, Tables_styles_templateObject10, Tables_styles_templateObject11, Tables_styles_templateObject12, Tables_styles_templateObject13;

var Tables_styles_Container = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject || (Tables_styles_templateObject = taggedTemplateLiteral_default()(["\n    min-width: 60vw;\n    position: relative;\n    display: flex;\n    gap: 10px;\n"])));
var styles_DateFormatNote = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject2 || (Tables_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    color: #666;\n"])));
var Tables_styles_LeftSide = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject3 || (Tables_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    width: 300px;\n    padding-right: 30px;\n    border-right: 1px solid #ccc;\n"])));
var Tables_styles_RightSide = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject4 || (Tables_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: calc(100% - 330px);\n"])));
var Tables_styles_Title = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject5 || (Tables_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    margin-bottom: 15px;\n"])));
var styles_TablesContainer = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject6 || (Tables_styles_templateObject6 = taggedTemplateLiteral_default()(["\n"])));
var TableHeader = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject7 || (Tables_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    padding-bottom: 12px;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    border-bottom: solid 2px #ccc;\n"])));
var TableBody = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject8 || (Tables_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    max-height: 270px;\n    overflow: auto;\n"])));
var Tables_styles_TableRow = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject9 || (Tables_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    padding: 10px 10px 10px 0px;\n    display: flex;\n    align-items: center;\n    justify-content: space-between;\n    border-bottom: solid 1px #ccc;\n"])));
var TableName = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject10 || (Tables_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n"])));
var Tables_styles_Actions = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject11 || (Tables_styles_templateObject11 = taggedTemplateLiteral_default()(["\n\n"])));
var Tables_styles_Icon = styled_components_browser_esm["b" /* default */].img(Tables_styles_templateObject12 || (Tables_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var TableFooter = styled_components_browser_esm["b" /* default */].div(Tables_styles_templateObject13 || (Tables_styles_templateObject13 = taggedTemplateLiteral_default()(["\n    font-size: 14px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    background-color: #f2f2f2;\n    padding: 10px;\n"])));
// CONCATENATED MODULE: ./src/images/filter.svg
/* harmony default export */ var images_filter = ("js/dist/2a0f0e6b15e81eeaa08daf2e86b5b6dc.svg");
// CONCATENATED MODULE: ./src/images/filter-after-change.svg
/* harmony default export */ var filter_after_change = ("js/dist/2cdcea9cad68e0fe239c57764a88c083.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Tables/index.tsx















function TableSubset(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    resetField = _useContext.resetField,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var source_environment_name = taskData.source_environment_name,
    environment_name = taskData.environment_name,
    parameters = taskData.parameters,
    tableList = taskData.tableList,
    subsetReset = taskData.subsetReset;
  var _useState = Object(react["useState"])(''),
    _useState2 = slicedToArray_default()(_useState, 2),
    selectedTable = _useState2[0],
    setSelectedTable = _useState2[1];
  Object(react["useEffect"])(function () {
    if (subsetReset) {
      setSelectedTable('');
      saveForm({
        subsetReset: false
      });
    }
  }, [subsetReset]);
  var _useState3 = Object(react["useState"])(null),
    _useState4 = slicedToArray_default()(_useState3, 2),
    parametersList = _useState4[0],
    setParametersList = _useState4[1];
  var _useState5 = Object(react["useState"])({
      group: {
        rules: [],
        operator: 'AND'
      }
    }),
    _useState6 = slicedToArray_default()(_useState5, 2),
    filter = _useState6[0],
    setFilter = _useState6[1];

  // useEffect(() => {
  //     let filter: FilterParamsItem | undefined = undefined;
  //     try {
  //         filter = JSON.parse(parameters || '');
  //     }
  //     catch (err: any) {
  //         console.error(err.message);
  //     }
  //     finally {
  //         if (!filter) {
  //             filter = {
  //                 group: {
  //                     rules: [],
  //                     operator: 'AND',
  //                 }
  //             }
  //         }
  //         else if (!filter.group) {
  //             filter.group = {
  //                 rules: [],
  //                 operator: 'AND',
  //             };
  //         }
  //         setFilter(filter);
  //     }
  // }, [parameters]);

  Object(react["useEffect"])(function () {
    var getData = /*#__PURE__*/function () {
      var _ref = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var tableData, data, result;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              if (selectedTable) {
                _context.next = 2;
                break;
              }
              return _context.abrupt("return");
            case 2:
              tableData = (tableList || []).find(function (it) {
                return it.reference_table_name === selectedTable;
              });
              if (tableData) {
                _context.next = 5;
                break;
              }
              return _context.abrupt("return");
            case 5:
              _context.next = 7;
              return apis_task.getTableParameters(tableData.interface_name, tableData.schema_name, tableData.reference_table_name);
            case 7:
              data = _context.sent;
              result = [];
              data.forEach(function (item) {
                result.push({
                  label: item.column_name,
                  value: item.column_name,
                  param_name: item.column_name,
                  name: item.column_name,
                  table: tableData.reference_table_name,
                  param_type: 'TEXT',
                  original_type: item.column_sqlite_type,
                  table_filter: true,
                  COMBO_INDICATOR: false,
                  valid_values: [],
                  min_value: 0,
                  max_value: 0
                });
              });
              if (tableData.gui_filter) {
                setFilter(JSON.parse(tableData.gui_filter));
              } else {
                setFilter({
                  group: {
                    rules: [],
                    operator: 'AND'
                  }
                });
              }
              setParametersList(result);
            case 12:
            case "end":
              return _context.stop();
          }
        }, _callee);
      }));
      return function getData() {
        return _ref.apply(this, arguments);
      };
    }();
    getData();
  }, [selectedTable]);
  var parametersDataChange = Object(react["useCallback"])(function () {
    var tableData = (tableList || []).find(function (it) {
      return it.reference_table_name === selectedTable;
    });
    if (!tableData) {
      return;
    }
    tableData.gui_filter = JSON.stringify(filter);
    tableData.filter_type = 'SQL';
    var sqlQueryData = getSelectionParamValue(filter, parametersList, 1);
    tableData.table_filter = sqlQueryData.sqlQuery === '()' ? null : sqlQueryData.sqlQuery;
    tableData.filter_parameters = sqlQueryData.values;
    tableData.filter_fields = sqlQueryData.filter_types;
    saveForm({
      tableList: toConsumableArray_default()(tableList || [])
    });
  }, [saveForm, tableList, selectedTable, filter]);
  var getTableRows = Object(react["useCallback"])(function () {
    return (tableList || []).map(function (it) {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Tables_styles_TableRow, {
        onClick: function onClick() {
          return setSelectedTable(it.reference_table_name);
        },
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TableName, {
          title: "Interface: ".concat(it.interface_name, ", schema: ").concat(it.schema_name, "."),
          children: it.reference_table_name
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Actions, {
          children: it.table_filter && it.table_filter !== '()' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Icon, {
            src: filter_after_change
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Icon, {
            src: images_filter
          })
        })]
      });
    });
  }, [tableList, setSelectedTable]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Tables_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Tables_styles_LeftSide, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Title, {
        children: "Filter tables data"
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TablesContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TableHeader, {
          children: "Table name"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TableBody, {
          children: getTableRows()
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableFooter, {
          children: ["Displaying ", (tableList || []).length, " tables"]
        })]
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_RightSide, {
      children: selectedTable ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Tables_styles_Title, {
          children: ["Table ", selectedTable, " filtering parameters"]
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_QueryBuilder, {
          parent: [],
          onChange: parametersDataChange,
          index: '0',
          parameters: parametersList,
          group: filter.group,
          type: 1
        })]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
    })]
  });
}
/* harmony default export */ var DataSubset_Tables = (TableSubset);
// CONCATENATED MODULE: ./src/components/FilterOutReserved/styles.ts

var FilterOutReserved_styles_templateObject, FilterOutReserved_styles_templateObject2, FilterOutReserved_styles_templateObject3;

var FilterOutReserved_styles_Container = styled_components_browser_esm["b" /* default */].div(FilterOutReserved_styles_templateObject || (FilterOutReserved_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 15px;\n"])));
var FilterOutReserved_styles_Title = styled_components_browser_esm["b" /* default */].div(FilterOutReserved_styles_templateObject2 || (FilterOutReserved_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    margin-bottom: 7px;\n"])));
var FilterOutReserved_styles_Body = styled_components_browser_esm["b" /* default */].div(FilterOutReserved_styles_templateObject3 || (FilterOutReserved_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    justify-content: flex-start;\n    gap: 10px;\n"])));
// CONCATENATED MODULE: ./src/components/FilterOutReserved/index.tsx






function FilterOutReserved() {
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    resetField = _useContext.resetField,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var filterout_reserved = taskData.filterout_reserved,
    replace_sequences = taskData.replace_sequences,
    clone_ind = taskData.clone_ind,
    target_env = taskData.target_env,
    environment_id = taskData.environment_id,
    load_entity = taskData.load_entity,
    sync_mode = taskData.sync_mode,
    version_ind = taskData.version_ind,
    selection_method = taskData.selection_method;
  var _onChange = Object(react["useCallback"])(function (value) {
    saveForm({
      filterout_reserved: value
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(FilterOutReserved_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(FilterOutReserved_styles_Title, {
      title: "Exclude entities reserved in the task's target testing environment",
      children: "Filter out reserved entities"
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FilterOutReserved_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: function onChange() {
          return _onChange('OTHERS');
        },
        name: "select_filter_out_reserved",
        value: 'OTHERS',
        selectedValue: '' + filterout_reserved,
        title: 'Reserved by others',
        disabled: (clone_ind || replace_sequences) && load_entity || target_env === 'ai_training' || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL'
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: function onChange() {
          return _onChange('ALL');
        },
        name: "select_filter_out_reserved",
        value: 'ALL',
        selectedValue: '' + filterout_reserved,
        title: 'All reserved entities',
        disabled: (clone_ind || replace_sequences) && load_entity || target_env === 'ai_training' || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL'
      })]
    })]
  });
}
/* harmony default export */ var components_FilterOutReserved = (FilterOutReserved);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/index.tsx



















var DataSubsetTypeEnum = /*#__PURE__*/function (DataSubsetTypeEnum) {
  DataSubsetTypeEnum["Entity"] = "Entity";
  DataSubsetTypeEnum["Tables"] = "Tables";
  return DataSubsetTypeEnum;
}({});
var SelectionMethodEnum = /*#__PURE__*/function (SelectionMethodEnum) {
  SelectionMethodEnum["L"] = "L";
  SelectionMethodEnum["ALL"] = "ALL";
  SelectionMethodEnum["C"] = "C";
  SelectionMethodEnum["CLONE"] = "CLONE";
  SelectionMethodEnum["R"] = "R";
  SelectionMethodEnum["P"] = "P";
  SelectionMethodEnum["PR"] = "PR";
  SelectionMethodEnum["AI_GENERATED"] = "AI_GENERATED";
  SelectionMethodEnum["GENERATE_SUBSET"] = "GENERATE_SUBSET";
  SelectionMethodEnum["TABLES"] = "TABLES";
  return SelectionMethodEnum;
}({});
function SubSetTypeTitle(props) {
  var icon = props.icon,
    text = props.text;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(DatasetIconContainer, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Icon, {
      src: icon
    }), text]
  });
}
var entitySeletionMethods = [{
  label: 'Entity list',
  value: 'L'
}, {
  label: 'Predefined entity list',
  value: 'ALL'
}, {
  label: 'Predefined custom logic',
  value: 'C'
}, {
  label: 'Business parameters',
  value: 'P'
}, {
  label: 'Random',
  value: 'R'
}];
function DataSubsetForm(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    unregister = _useContext.unregister,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var dataSubsetType = taskData.dataSubsetType,
    selection_method = taskData.selection_method,
    version_ind = taskData.version_ind,
    sync_mode = taskData.sync_mode,
    generation_type = taskData.generation_type,
    dataSourceType = taskData.dataSourceType,
    synthetic_type = taskData.synthetic_type,
    source_type = taskData.source_type,
    userRole = taskData.userRole,
    sourceUserRole = taskData.sourceUserRole,
    isCoupling = taskData.isCoupling;
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    localSelectionMethod = _useState2[0],
    setLocalSelectionMethod = _useState2[1];
  Object(react["useEffect"])(function () {
    if (selection_method === 'TABLES') {
      return;
    }
    var updateData = {};
    if (!generation_type) {
      updateData.generation_type = 'all';
    }
    if (sync_mode === 'OFF' && version_ind) {
      if (['L', 'ALL'].indexOf(selection_method || '') < 0) {
        updateData.selection_method = 'L';
      }
    } else if (!selection_method) {
      updateData.selection_method = 'L';
    }
    saveForm(updateData);
  }, []);
  var entitySelectionMethodOptions = Object(react["useMemo"])(function () {
    var result = entitySeletionMethods;
    var changedLabel = false;
    if (sync_mode === 'OFF' && version_ind) {
      result = result.filter(function (it) {
        return it.value === 'L' || it.value === 'ALL';
      });
      var _found = result.find(function (it) {
        return it.value === 'ALL';
      });
      if (_found) {
        changedLabel = true;
        _found.label = 'Select all entities of the selected version';
      }
    } else if (dataSourceType !== 'data_source' && synthetic_type === 'generated_data') {
      result = result.filter(function (it) {
        return it.value !== 'L' && it.value !== 'ALL';
      });
      if (!isCoupling && dataSourceType === 'ai_generated') {
        result = result.filter(function (it) {
          return it.value !== 'P';
        });
      }
    }
    if (!((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || (!userRole || userRole !== null && userRole !== void 0 && userRole.allowed_random_entity_selection) && (!sourceUserRole || sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_random_entity_selection) && (userRole || sourceUserRole))) {
      result = result.filter(function (it) {
        return it.value !== 'R';
      });
    }
    if (sync_mode !== 'OFF' || !version_ind) {
      if ((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'tester') {
        result = result.filter(function (it) {
          return it.value !== 'ALL';
        });
      }
    }
    var found = result.find(function (it) {
      return it.value === 'ALL';
    });
    if (found && !changedLabel) {
      found.label = 'Predefined entity list';
    }
    return result;
  }, [sync_mode, version_ind, dataSourceType, synthetic_type, userRole, sourceUserRole, systemUserRole]);
  Object(react["useEffect"])(function () {
    // TODO Save Data
    var temp = selection_method || 'L';
    if (selection_method === 'PR') {
      temp = 'P';
    }
    var found = entitySelectionMethodOptions.find(function (it) {
      return it.value === temp;
    });
    if (found) {
      setLocalSelectionMethod(found);
    }
  }, [selection_method, entitySelectionMethodOptions]);
  var getSelectionMethodBody = Object(react["useCallback"])(function () {
    if (!localSelectionMethod) {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
    }
    switch (localSelectionMethod.value) {
      case SelectionMethodEnum.L:
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_EntityList, {});
      case SelectionMethodEnum.C:
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_CustomLogic, {});
      case SelectionMethodEnum.R:
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
          width: '300px',
          title: "Number of entities"
        });
      case SelectionMethodEnum.PR:
      case SelectionMethodEnum.P:
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_Parameters, {});
    }
  }, [localSelectionMethod]);
  var selectionMethodChange = Object(react["useCallback"])(function (item) {
    setLocalSelectionMethod(item);
    unregister('selection_param_value');
    saveForm({
      selection_method: item.value,
      selection_param_value: undefined
    });
  }, [saveForm]);
  var generationTypeChange = Object(react["useCallback"])(function (value) {
    saveForm({
      generation_type: value
    });
  }, [saveForm]);
  var parametersRandomChange = Object(react["useCallback"])(function (value) {
    saveForm({
      selection_method: value ? 'PR' : 'P'
    });
  }, [saveForm]);
  var getSelectionMethodSelectRightSide = Object(react["useCallback"])(function () {
    if (!localSelectionMethod) {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
    }
    switch (localSelectionMethod.value) {
      case SelectionMethodEnum.L:
      case SelectionMethodEnum.C:
      case SelectionMethodEnum.ALL:
      case SelectionMethodEnum.R:
        return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FilterOutReserved, {})]
        });
      case SelectionMethodEnum.PR:
      case SelectionMethodEnum.P:
        return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Seprator, {
            expand: true
          }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(NumberOfEntitiesContainer, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
              title: 'Number of entities in subset',
              width: '300px'
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
              title: 'Use parameters with random selection',
              name: "random_parameters",
              value: selection_method === 'PR',
              onChange: parametersRandomChange
            })]
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Seprator, {
            expand: true
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FilterOutReserved, {})]
        });
    }
  }, [localSelectionMethod, parametersRandomChange, selection_method]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(DataSubset_styles_Wrapper, {
    children: [dataSourceType !== 'data_source' && synthetic_type === 'generated_data' ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(GenerationTypeOptions, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: generationTypeChange,
        name: "generation_type",
        value: "all",
        selectedValue: generation_type,
        title: 'Load all generated entities of a selected data generation execution'
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: generationTypeChange,
        name: "generation_type",
        value: "partial",
        selectedValue: generation_type,
        title: 'Load a partial entity subset'
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_Tables, {}) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
      children: [!(dataSourceType !== 'data_source' && synthetic_type === 'generated_data') || generation_type === 'partial' ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(SelectMethodSelectContainer, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
            width: '300px',
            title: 'Select subsetting method',
            placeholder: 'Select Method',
            mandatory: true,
            value: localSelectionMethod,
            options: entitySelectionMethodOptions,
            loading: false,
            onChange: selectionMethodChange
          }), getSelectionMethodSelectRightSide()]
        }), getSelectionMethodBody()]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), version_ind && sync_mode === 'OFF' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DataVersioningContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectDataVerioning, {})
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), dataSourceType !== 'data_source' && synthetic_type === 'generated_data' && generation_type === 'all' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(DataGenerationContainer, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_SelectGeneratedExecution, {
        dataSourceType: dataSourceType
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var DataSubset = (DataSubsetForm);
// CONCATENATED MODULE: ./src/containers/Task/Froms/TestDataStore/styles.ts

var Froms_TestDataStore_styles_templateObject, Froms_TestDataStore_styles_templateObject2, Froms_TestDataStore_styles_templateObject3, Froms_TestDataStore_styles_templateObject4, Froms_TestDataStore_styles_templateObject5, Froms_TestDataStore_styles_templateObject6;

var TestDataStore_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject || (Froms_TestDataStore_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: center;\n"])));
var TestDataStore_styles_Container = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject2 || (Froms_TestDataStore_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display:flex;\n    flex-direction: column;\n    align-items:flex-start;\n    gap: 33px;\n    justify-content: space-between;\n"])));
var RefreshDataContainer = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject3 || (Froms_TestDataStore_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    margin-top: 24px;\n    display: flex;\n    flex-direction: column;\n    gap: 19px;\n"])));
var RetentionPeriodContainer = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject4 || (Froms_TestDataStore_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n"])));
var styles_DataVersioningContainer = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject5 || (Froms_TestDataStore_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    margin-top: 33px;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n"])));
var TestDataStore_styles_Title = styled_components_browser_esm["b" /* default */].div(Froms_TestDataStore_styles_templateObject6 || (Froms_TestDataStore_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    color: #2e2e2e;\n"])));
// CONCATENATED MODULE: ./src/components/Periods/styles.ts

var Periods_styles_templateObject;

var Periods_styles_Container = styled_components_browser_esm["b" /* default */].div(Periods_styles_templateObject || (Periods_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    cursor: pointer;\n    gap: 20px;\n    align-items: flex-end;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Main/usePeriods.ts








var PeriodUnitType = /*#__PURE__*/function (PeriodUnitType) {
  PeriodUnitType["Minutes"] = "Minutes";
  PeriodUnitType["Hours"] = "Hours";
  PeriodUnitType["Days"] = "Days";
  PeriodUnitType["Weeks"] = "Weeks";
  PeriodUnitType["Years"] = "Years";
  PeriodUnitType["Do_Not_Delete"] = "Do Not Delete";
  PeriodUnitType["Do_Not_Retain"] = "Do Not Retain";
  return PeriodUnitType;
}({});
var usePeriods_usePeriods = function usePeriods(saveForm, version_ind, dataSourceType, source_type, retention_period_value, retention_period_type, reserve_retention_period_value) {
  var AuthService = getService('AuthService');
  var prevDataSourceType = Object(usehooks["c" /* usePrevious */])(dataSourceType);
  var previousSource_type = Object(usehooks["c" /* usePrevious */])(source_type);
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    periodsData = _useState2[0],
    setPeriodsData = _useState2[1];
  var updatePeriods = Object(react["useCallback"])(function (onLoad) {
    if (!periodsData) {
      return;
    }
    var updateData = {};
    var role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
    var periodTypes = toConsumableArray_default()(periodsData === null || periodsData === void 0 ? void 0 : periodsData.retentionPeriodTypes) || [];
    var reservationPeriodTypes = toConsumableArray_default()(periodsData === null || periodsData === void 0 ? void 0 : periodsData.reservationPeriodTypes) || [];
    var maxRetentionPeriod = periodsData.maxRetentionPeriod;
    var maxReservationPeriod = periodsData.maxReservationPeriod;
    var retentionDefaultPeriod = periodsData.retentionDefaultPeriod;
    var reservationDefaultPeriod = periodsData.reservationDefaultPeriod;
    var versioningPeriod = function versioningPeriod(isTester) {
      var _retentionDefaultPeri;
      if (isTester) {
        retentionDefaultPeriod = periodsData === null || periodsData === void 0 ? void 0 : periodsData.versioningRetentionPeriodForTesters;
      } else {
        retentionDefaultPeriod = periodsData === null || periodsData === void 0 ? void 0 : periodsData.versioningRetentionPeriod;
      }
      periodTypes.unshift({
        name: PeriodUnitType.Do_Not_Retain,
        units: -1,
        label: 'Do not retain',
        value: 'Do Not Retain'
      });
      if ((_retentionDefaultPeri = retentionDefaultPeriod) !== null && _retentionDefaultPeri !== void 0 && _retentionDefaultPeri.allow_doNotDelete) {
        if (dataSourceType === 'data_source' && source_type === 'tables') {
          retentionDefaultPeriod = {
            "units": PeriodUnitType.Do_Not_Delete,
            "value": -1
          };
        }
        periodTypes.unshift({
          name: PeriodUnitType.Do_Not_Delete,
          units: -1,
          label: 'Do not delete',
          value: 'Do Not Delete'
        });
      }
    };
    if (role && role.type === 'tester') {
      maxRetentionPeriod = periodsData === null || periodsData === void 0 ? void 0 : periodsData.maxRetentionPeriodForTesters;
      maxReservationPeriod = periodsData === null || periodsData === void 0 ? void 0 : periodsData.maxReservationPeriodForTesters;
      if (version_ind) {
        versioningPeriod(true);
      } else {
        periodTypes.unshift({
          name: PeriodUnitType.Do_Not_Retain,
          units: 0,
          label: 'Do not retain',
          value: 'Do Not Retain'
        });
        periodTypes.unshift({
          name: PeriodUnitType.Do_Not_Delete,
          units: -1,
          label: 'Do not delete',
          value: 'Do Not Delete'
        });
      }
    } else {
      if (version_ind) {
        versioningPeriod();
      } else {
        periodTypes.unshift({
          name: PeriodUnitType.Do_Not_Retain,
          units: 0,
          label: 'Do not retain',
          value: 'Do Not Retain'
        });
        periodTypes.unshift({
          name: PeriodUnitType.Do_Not_Delete,
          units: -1,
          label: 'Do not delete',
          value: 'Do Not Delete'
        });
      }
    }
    if (maxRetentionPeriod && maxRetentionPeriod.value) {
      periodTypes = periodTypes.filter(function (period) {
        return period.units <= maxRetentionPeriod.value;
      });
    }
    if (maxReservationPeriod && maxReservationPeriod.value) {
      reservationPeriodTypes = reservationPeriodTypes.filter(function (period) {
        return period.units <= maxReservationPeriod.value;
      });
    }
    updateData.periodTypes = periodTypes;
    updateData.reservationPeriodTypes = reservationPeriodTypes;
    updateData.maxReservationPeriod = maxReservationPeriod;
    updateData.maxRetentionPeriod = maxRetentionPeriod;
    if (onLoad && retention_period_value === undefined) {
      if (retentionDefaultPeriod) {
        updateData.retention_period_type = retentionDefaultPeriod.units;
        updateData.retention_period_value = retentionDefaultPeriod.value;
      }
    }
    if (onLoad && reserve_retention_period_value === undefined) {
      if (reservationDefaultPeriod) {
        updateData.reserve_retention_period_type = reservationDefaultPeriod.units;
        updateData.reserve_retention_period_value = reservationDefaultPeriod.value;
      }
    }
    saveForm(updateData);
  }, [saveForm, version_ind, periodsData, AuthService, reserve_retention_period_value, retention_period_value, dataSourceType, source_type]);
  Object(react["useEffect"])(function () {
    updatePeriods(true);
  }, [periodsData]);
  Object(react["useEffect"])(function () {
    if (retention_period_type === 'reset') {
      updatePeriods();
    }
  }, [retention_period_type]);
  Object(react["useEffect"])(function () {
    function fetchReservationPeriodsData() {
      return _fetchReservationPeriodsData.apply(this, arguments);
    }
    function _fetchReservationPeriodsData() {
      _fetchReservationPeriodsData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.next = 2;
              return apis_task.getRetentionPeriodsData();
            case 2:
              data = _context.sent;
              data.reservationPeriodTypes.forEach(function (option) {
                option.label = option.name;
                option.value = option.units;
              });
              data.retentionPeriodTypes.forEach(function (option) {
                option.label = option.name;
                option.value = option.units;
              });
              setPeriodsData(data);
            case 6:
            case "end":
              return _context.stop();
          }
        }, _callee);
      }));
      return _fetchReservationPeriodsData.apply(this, arguments);
    }
    fetchReservationPeriodsData();
  }, []);
  Object(react["useEffect"])(function () {
    updatePeriods();
  }, [version_ind]);
  Object(react["useEffect"])(function () {
    if (retention_period_value === 0) {
      saveForm({
        version_ind: false
      });
    } else if (retention_period_value === -1 && dataSourceType === 'data_source' && source_type === 'tables') {
      saveForm({
        version_ind: true
      });
    }
  }, [retention_period_value]);
  Object(react["useEffect"])(function () {
    if (prevDataSourceType === dataSourceType && previousSource_type === source_type || !prevDataSourceType || !dataSourceType) {
      return;
    }
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      saveForm({
        version_ind: true
      });
    } else {
      saveForm({
        version_ind: false
      });
    }
  }, [dataSourceType, source_type]);

  // need to add code for tester
};
/* harmony default export */ var Main_usePeriods = (usePeriods_usePeriods);
// CONCATENATED MODULE: ./src/components/Periods/index.tsx


function Periods_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Periods_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Periods_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Periods_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }










var retentionMap = {
  periodTypes: 'retentionPeriodTypes',
  maxPeriod: 'maxRetentionPeriod',
  defaultPeriod: 'retentionDefaultPeriod',
  testersPeriods: 'retentionPeriodForTesters',
  versioning: 'versioningRetentionPeriod',
  period_type: 'retention_period_type',
  period_value: 'retention_period_value'
};
var reserveMap = {
  periodTypes: 'reservationPeriodTypes',
  maxPeriod: 'maxReservationPeriod',
  defaultPeriod: 'reservationDefaultPeriod',
  testersPeriods: 'maxReservationPeriodForTesters',
  versioning: 'versioningRetentionPeriod',
  period_type: 'reserve_retention_period_type',
  period_value: 'reserve_retention_period_value'
};
function Periods(props) {
  var _errors;
  var title = props.title,
    mandatory = props.mandatory,
    period_type = props.period_type,
    onChange = props.onChange,
    period_value = props.period_value,
    periodsData = props.periodsData,
    maxPeriod = props.maxPeriod,
    disabled = props.disabled,
    reserve = props.reserve;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    errors = _useContext.errors;
  var _useState = Object(react["useState"])(periodsData || []),
    _useState2 = slicedToArray_default()(_useState, 2),
    localOptions = _useState2[0],
    setLocalOptions = _useState2[1];
  var _useState3 = Object(react["useState"])(retentionMap),
    _useState4 = slicedToArray_default()(_useState3, 2),
    periodFields = _useState4[0],
    setPeriodFields = _useState4[1];
  var _useState5 = Object(react["useState"])(),
    _useState6 = slicedToArray_default()(_useState5, 2),
    selectedPeriodType = _useState6[0],
    setSelectedPeriodType = _useState6[1];
  Object(react["useEffect"])(function () {
    if (reserve) {
      setPeriodFields(reserveMap);
    } else {
      setPeriodFields(retentionMap);
    }
  }, [reserve]);
  var _useState7 = Object(react["useState"])(Infinity),
    _useState8 = slicedToArray_default()(_useState7, 2),
    maxPeriodLocal = _useState8[0],
    setMaxPeriodLocal = _useState8[1];
  Object(react["useEffect"])(function () {
    if (selectedPeriodType && maxPeriod) {
      setMaxPeriodLocal(maxPeriod.value / selectedPeriodType.units);
    }
  }, [selectedPeriodType, maxPeriod]);
  Object(react["useEffect"])(function () {
    if (period_type) {
      var found = localOptions.find(function (it) {
        return it.name === period_type;
      });
      if (found) {
        setSelectedPeriodType(found);
      }
    }
  }, [localOptions, period_type]);
  console.log(period_type);
  Object(react["useEffect"])(function () {
    if (periodsData) {
      setLocalOptions(periodsData);
    }
  }, [periodsData]);
  var periodTypeChange = Object(react["useCallback"])(function (option) {
    if (option.name === PeriodUnitType.Do_Not_Delete || option.name === PeriodUnitType.Do_Not_Retain) {
      var _onChange;
      onChange((_onChange = {}, defineProperty_default()(_onChange, periodFields.period_type, option.name), defineProperty_default()(_onChange, periodFields.period_value, option.name === PeriodUnitType.Do_Not_Delete ? -1 : 0), _onChange));
    } else {
      var _onChange2;
      onChange((_onChange2 = {}, defineProperty_default()(_onChange2, periodFields.period_type, option.name), defineProperty_default()(_onChange2, periodFields.period_value, 1), _onChange2));
    }
  }, [onChange, periodFields]);
  var periodValueChange = Object(react["useCallback"])(function (value) {
    onChange(defineProperty_default()({}, periodFields.period_value, value));
  }, [onChange, periodFields]);
  var checkPeriodsValue = function checkPeriodsValue(value, taskData) {
    var field_value = taskData["".concat(reserve ? 'reserve_retention' : 'retention', "_period_value")];
    var field_type = taskData["".concat(reserve ? 'reserve_retention' : 'retention', "_period_type")];
    if (field_value !== undefined) {
      if (field_value > maxPeriodLocal) {
        return "Selected retention period (".concat(((selectedPeriodType === null || selectedPeriodType === void 0 ? void 0 : selectedPeriodType.units) || 0) * (period_value || 0), ") cannot exceed ").concat(maxPeriod === null || maxPeriod === void 0 ? void 0 : maxPeriod.value, " days");
      } else if (!reserve && field_value < 1 && field_type !== 'Do Not Delete' && field_type !== 'Do Not Retain') {
        return 'The retention period must be bigger than zero';
      } else if (reserve && field_value < 1 && (systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) !== 'admin') {
        return 'The reservation period must be bigger than zero';
      }
      return true;
    } else if (field_type === 'Do Not Delete' || field_type === 'Do Not Retain') {
      return true;
    }
    if (reserve) {
      return 'Set a reservation period';
    }
    return 'Please input retention period value';
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Periods_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
      disabled: disabled,
      width: "290px",
      title: title,
      mandatory: false,
      options: localOptions,
      value: selectedPeriodType,
      onChange: periodTypeChange,
      error: (_errors = errors["".concat(reserve ? 'reserve_retenion' : 'retenion', "_period_value")]) === null || _errors === void 0 ? void 0 : _errors.message
    }), period_type !== 'Do Not Delete' && period_type !== 'Do Not Retain' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, Periods_objectSpread(Periods_objectSpread({}, register("".concat(reserve ? 'reserve_retenion' : 'retenion', "_period_value"), {
      value: period_value,
      validate: defineProperty_default()({}, "".concat(reserve ? 'reserve_' : '', "retention_value"), checkPeriodsValue)
    })), {}, {
      disabled: disabled,
      width: "60px",
      title: "",
      mandatory: mandatory,
      type: InputTypes.number,
      name: "".concat(reserve ? 'reserve_retenion' : 'retenion', "_period_value"),
      value: period_value,
      min: (systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' && reserve ? 0 : 1,
      max: maxPeriodLocal,
      onChange: periodValueChange
    })) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var components_Periods = (Periods);
// CONCATENATED MODULE: ./src/containers/Task/Froms/TestDataStore/index.tsx








function TestDataStoreForm(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var version_ind = taskData.version_ind,
    sync_mode = taskData.sync_mode,
    retention_period_type = taskData.retention_period_type,
    retention_period_value = taskData.retention_period_value,
    dataSourceType = taskData.dataSourceType,
    maxRetentionPeriod = taskData.maxRetentionPeriod,
    periodTypes = taskData.periodTypes,
    synthetic_type = taskData.synthetic_type,
    source_type = taskData.source_type,
    userRole = taskData.userRole,
    sourceUserRole = taskData.sourceUserRole;
  var onDataVersioningchange = Object(react["useCallback"])(function (value) {
    saveForm({
      version_ind: value || false
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TestDataStore_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TestDataStore_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
        name: "data_versioning",
        title: "Create data snapshot (version)",
        value: version_ind && !(sync_mode === 'OFF' && dataSourceType === 'data_source'),
        onChange: onDataVersioningchange,
        disabled: !((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || (!userRole || userRole !== null && userRole !== void 0 && userRole.allowed_entity_versioning) && (!sourceUserRole || sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_entity_versioning) && (userRole || sourceUserRole)) || sync_mode === 'OFF' && dataSourceType === 'data_source' || dataSourceType !== 'data_source' && synthetic_type === 'generated_data' || dataSourceType === 'data_source' && source_type === 'tables' || retention_period_type === 'Do Not Retain'
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(RetentionPeriodContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Periods, {
          disabled: sync_mode === 'OFF' && dataSourceType === 'data_source' || dataSourceType !== 'data_source' && synthetic_type === 'generated_data',
          title: 'Retention period',
          mandatory: true,
          period_type: retention_period_type,
          period_value: retention_period_value,
          maxPeriod: maxRetentionPeriod,
          periodsData: periodTypes,
          onChange: saveForm
        })
      })]
    })
  });
}
/* harmony default export */ var Froms_TestDataStore = (TestDataStoreForm);
// CONCATENATED MODULE: ./src/containers/Task/Main/useWidgetStatus.ts



function useWidgetStatus_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function useWidgetStatus_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? useWidgetStatus_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : useWidgetStatus_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }




var useWidgetStatus_useWidgetStatus = function useWidgetStatus(taskData, trigger, isValid, handleSubmit, failedComp) {
  var _useState = Object(react["useState"])(true),
    _useState2 = slicedToArray_default()(_useState, 2),
    initTask = _useState2[0],
    setInitTask = _useState2[1];
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var _useState3 = Object(react["useState"])('task_title'),
    _useState4 = slicedToArray_default()(_useState3, 2),
    currentStep = _useState4[0],
    setCurrentStep = _useState4[1];
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    touchedForms = _useState6[0],
    setTouchedForms = _useState6[1];
  var _useState7 = Object(react["useState"])(true),
    _useState8 = slicedToArray_default()(_useState7, 2),
    submittedForm = _useState8[0],
    setSubmittedForm = _useState8[1];
  var _useState9 = Object(react["useState"])({
      dataSourceStatus: StatusEnum.disabled,
      subsetStatus: StatusEnum.disabled,
      subsetPosition: SubsetPossition.undefined,
      testDataStoreStatus: StatusEnum.disabled,
      targetStatus: StatusEnum.disabled,
      isTargetConnectionEnabled: false,
      isSourceConnectionEnabled: false
    }),
    _useState10 = slicedToArray_default()(_useState9, 2),
    statuses = _useState10[0],
    setStatuses = _useState10[1];

  // useEffect(() => {
  //     console.log(statuses);
  // }, [statuses]);

  var statusesMap = Object(react["useMemo"])(function () {
    return {
      source: 'dataSourceStatus',
      source_data_subset: 'sourceSubsetStatus',
      target_data_subset: 'targetSubsetStatus',
      test_data_store: 'testDataStoreStatus',
      target: 'targetStatus'
    };
  }, []);
  var getSourceStatus = function getSourceStatus(taskData) {
    if (failedComp === 'source') {
      return StatusEnum.blink;
    }
    if (!taskData || taskData.reserve_ind && !taskData.load_entity || taskData.delete_before_load && !taskData.load_entity) {
      return StatusEnum.disabled;
    }
    if (taskData.dataSourceType === 'data_source' && taskData.source_environment_id && (taskData.source_type === 'BE' && taskData.be_id || taskData.source_type === 'tables')) {
      return StatusEnum.completed;
    } else if (taskData.dataSourceType !== 'data_source' && taskData.be_id) {
      if (taskData.synthetic_type === 'generated_data') {
        return StatusEnum.completed;
      }
      if (taskData.num_of_entities && taskData.num_of_entities > 0 && (taskData.dataSourceType === 'ai_generated' && taskData.selected_subset_task_exe_id || taskData.dataSourceType === 'synthetic')) {
        return StatusEnum.completed;
      }
    }
    if (!(taskData.dataSourceType === 'data_source' && !taskData.source_environment_id && !taskData.mask_sensitive_data)) {
      if (touchedForms.indexOf('source') >= 0) {
        return StatusEnum.partial;
      }
    }
    return StatusEnum.enabled;
  };
  var getTestDataStoreStatus = function getTestDataStoreStatus(taskData) {
    if (taskData.sync_mode === 'OFF' && taskData.dataSourceType === 'data_source') {
      return StatusEnum.completed;
    }
    if (taskData.source_environment_id || taskData.environment_id || taskData.dataSourceType === 'synthetic' || taskData.dataSourceType === 'ai_generated') {
      if (['Do Not Delete', 'Do Not Retain'].indexOf(taskData.retention_period_type || '') >= 0) {
        return StatusEnum.completed;
      } else if ((taskData.retention_period_value || 0) > 0) {
        return StatusEnum.completed;
      }
    }
    return StatusEnum.enabled;
  };
  var getTargetStatus = function getTargetStatus(taskData, statuses, currentStep) {
    var sync_mode = taskData.sync_mode,
      version_ind = taskData.version_ind,
      dataSourceType = taskData.dataSourceType,
      source_type = taskData.source_type;
    if (sync_mode !== 'OFF' && version_ind && !(dataSourceType === 'data_source' && source_type === 'tables')) {
      return StatusEnum.disabled;
    }
    if (failedComp === 'target') {
      return StatusEnum.blink;
    }
    if (currentStep !== 'target' && (statuses === null || statuses === void 0 ? void 0 : statuses.targetStatus) !== StatusEnum.completed && (statuses === null || statuses === void 0 ? void 0 : statuses.subsetPosition) === SubsetPossition.target) {
      return StatusEnum.blink;
    }
    if (taskData.environment_id && (taskData.load_entity && (taskData.clone_ind && taskData.num_of_clones || !taskData.clone_ind) || taskData.delete_before_load || taskData.reserve_ind)) {
      if (taskData.reserve_ind && (!taskData.reserve_retention_period_type || (systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) !== 'admin' && (taskData.reserve_retention_period_value || 0) <= 0 || (systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' && taskData.reserve_retention_period_value === undefined)) {
        return StatusEnum.partial;
      }
      return StatusEnum.completed;
    }
    if (taskData.target_env === 'ai_training') {
      return StatusEnum.completed;
    }
    if (!(taskData.target_env === 'target_env' && !taskData.environment_id && !taskData.load_entity && !taskData.delete_before_load && !taskData.reserve_ind)) {
      if (touchedForms.indexOf('target') >= 0 && !(taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables')) {
        return StatusEnum.partial;
      }
    }
    return StatusEnum.enabled;
  };
  var subsetStatus = function subsetStatus(taskData) {
    var sourceStatus = getSourceStatus(taskData);
    var targetStatus = getTargetStatus(taskData, null, '');
    if (failedComp.indexOf('subset') >= 0) {
      return StatusEnum.blink;
    }
    var checkVersioningStatus = function checkVersioningStatus() {
      if (taskData.version_ind && taskData.sync_mode === 'OFF') {
        if (!taskData.selected_version_task_exe_id) {
          return StatusEnum.enabled;
        }
      }
      return StatusEnum.completed;
    };
    if (sourceStatus === StatusEnum.completed || targetStatus === StatusEnum.completed) {
      if (taskData.selection_method === 'TABLES') {
        return StatusEnum.completed;
      } else if (taskData.selection_method === 'L' && taskData.selection_param_value) {
        return checkVersioningStatus();
      } else if (taskData.selection_method === 'ALL') {
        return checkVersioningStatus();
      } else if (taskData.selection_method === 'R' && (taskData.num_of_entities || taskData.clone_ind)) {
        return StatusEnum.completed;
      } else if ((taskData.selection_method === 'P' || taskData.selection_method === 'PR') && taskData.selection_param_value && (taskData.num_of_entities || taskData.clone_ind)) {
        return StatusEnum.completed;
      } else if (taskData.selection_method === 'C') {
        return StatusEnum.completed;
      } else if (taskData.generation_type === 'all' && taskData.selected_subset_task_exe_id) {
        return StatusEnum.completed;
      }
      return StatusEnum.enabled;
    }
    return StatusEnum.disabled;
  };
  var subsetPosition = function subsetPosition(taskData) {
    if (taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'new_data') {
      return SubsetPossition.undefined;
    }
    if (taskData.dataSourceType === 'data_source' && taskData.sync_mode === 'OFF' || taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'generated_data' || taskData.reserve_ind && !taskData.load_entity || taskData.delete_before_load && !taskData.load_entity) {
      return SubsetPossition.target;
    }
    return SubsetPossition.source;
  };
  var getSourceSubsetStatus = function getSourceSubsetStatus(taskData) {
    if (subsetPosition(taskData) === SubsetPossition.source) {
      return subsetStatus(taskData);
    }
    return StatusEnum.disabled;
  };
  var getTargetSubsetStatus = function getTargetSubsetStatus(taskData) {
    if (subsetPosition(taskData) === SubsetPossition.target) {
      return subsetStatus(taskData);
    }
    return StatusEnum.disabled;
  };
  var isTargetConnectionEnabled = function isTargetConnectionEnabled(taskData) {
    var targetStatus = getTargetStatus(taskData, null, '');
    var subsetStatusValue = subsetStatus(taskData);
    var subsetPositionValue = subsetPosition(taskData);
    if (targetStatus === StatusEnum.partial || targetStatus === StatusEnum.completed || currentStep === 'target') {
      return true;
    }
    if (subsetStatusValue === StatusEnum.enabled) {
      if (subsetPositionValue === SubsetPossition.source) {
        return false;
      }
    }
    if (!taskData.load_entity && taskData.reserve_ind) {
      return true;
    }
    return false;
  };
  var isSourceConnectionEnabled = function isSourceConnectionEnabled(taskData) {
    var sourceStatus = getSourceStatus(taskData);
    var subsetStatusValue = subsetStatus(taskData);
    var subsetPositionValue = subsetPosition(taskData);
    if (subsetPositionValue === SubsetPossition.target) {
      return false;
    }
    if (sourceStatus === StatusEnum.disabled) {
      return false;
    }
    if (!taskData.load_entity && taskData.reserve_ind) {
      return false;
    }
    if (sourceStatus === StatusEnum.partial || sourceStatus === StatusEnum.completed || currentStep === 'source') {
      return true;
    }
    return false;
  };
  var statusesFuncMap = Object(react["useMemo"])(function () {
    return {
      dataSourceStatus: getSourceStatus,
      sourceSubsetStatus: getSourceSubsetStatus,
      targetSubsetStatus: getTargetSubsetStatus,
      testDataStoreStatus: getTestDataStoreStatus,
      targetStatus: getTargetStatus,
      isTargetConnectionEnabled: isTargetConnectionEnabled,
      isSourceConnectionEnabled: isSourceConnectionEnabled,
      subsetStatus: subsetStatus,
      subsetPosition: subsetPosition
    };
  }, [currentStep, failedComp]);
  var updateWidgetStatuses = Object(react["useCallback"])(function (taskData, currentStep) {
    var statusesTemp = useWidgetStatus_objectSpread({}, statuses);
    Object.keys(statusesFuncMap).forEach(function (statusName) {
      statusesTemp[statusName] = statusesFuncMap[statusName](taskData, statusesTemp);
    });
    statusesTemp.targetStatus = statusesFuncMap.targetStatus(taskData, statusesTemp, currentStep);
    setStatuses(statusesTemp);
  }, [statuses, statusesFuncMap, touchedForms]);
  var _useState11 = Object(react["useState"])(''),
    _useState12 = slicedToArray_default()(_useState11, 2),
    pendingStep = _useState12[0],
    setPendingStep = _useState12[1];
  var moveToStepAfterValidaity = Object(react["useCallback"])(function () {
    var stepTemp = '';
    stepTemp = statusesMap[pendingStep];
    if (stepTemp) {
      if (statuses[stepTemp] !== StatusEnum.disabled) {
        setCurrentStep(pendingStep);
      } else if (statuses[statusesMap[pendingStep]] === StatusEnum.disabled) {
        //TODO show error message
      }
    } else {
      setCurrentStep(pendingStep);
    }
    setSubmittedForm(true);
    setPendingStep('');
  }, [statuses, statusesMap, pendingStep]);
  var formOnError = Object(react["useCallback"])(function (data) {
    Object.keys(data).forEach(function (key) {
      if (!data[key].ref.step) {
        data[key].ref.step = currentStep;
      }
    });
    console.error(data);
    moveToStepAfterValidaity();
    return;
    if (stepsConfig[currentStep].mandatoryFields && stepsConfig[currentStep].mandatoryFields.length > 0) {
      var fields = Object.keys(data);
      var filteredArray = fields.filter(function (value) {
        return stepsConfig[currentStep].mandatoryFields.includes(value);
      });
      if (filteredArray.length === 0) {
        moveToStepAfterValidaity();
        return;
      }
    }
    console.error(data);
    setPendingStep('');
  }, [pendingStep, currentStep]);
  var checkFormMandatory = Object(react["useCallback"])(function () {
    return stepsConfig[currentStep] && stepsConfig[currentStep].mandatory;
  }, [currentStep]);
  Object(react["useEffect"])(function () {
    if (pendingStep) {
      if (checkFormMandatory()) {
        handleSubmit(moveToStepAfterValidaity, formOnError)();
      } else {
        moveToStepAfterValidaity();
      }
    }
  }, [pendingStep, handleSubmit, moveToStepAfterValidaity, formOnError, checkFormMandatory]);
  var onClickStep = Object(react["useCallback"])(function (step) {
    setInitTask(false);
    if (!currentStep) {
      setCurrentStep(step);
      return;
    }
    if (currentStep && touchedForms.indexOf(currentStep) < 0) {
      setTouchedForms([].concat(toConsumableArray_default()(touchedForms), [currentStep]));
    }
    if (step !== currentStep) {
      setPendingStep(step);
    }
  }, [currentStep, touchedForms, setInitTask]);
  Object(react["useEffect"])(function () {
    updateWidgetStatuses(taskData, currentStep);
  }, [taskData, currentStep]);
  return {
    statuses: statuses,
    onClickStep: onClickStep,
    currentStep: currentStep,
    touchedForms: touchedForms,
    setTouchedForms: setTouchedForms,
    submittedForm: submittedForm,
    setSubmittedForm: setSubmittedForm,
    statusesFuncMap: statusesFuncMap,
    initTask: initTask
  };
};
/* harmony default export */ var Main_useWidgetStatus = (useWidgetStatus_useWidgetStatus);
// CONCATENATED MODULE: ./src/components/RadioGroup/styles.ts

var RadioGroup_styles_templateObject, RadioGroup_styles_templateObject2, RadioGroup_styles_templateObject3, RadioGroup_styles_templateObject4, RadioGroup_styles_templateObject5;

var RadioGroup_styles_Container = styled_components_browser_esm["b" /* default */].div(RadioGroup_styles_templateObject || (RadioGroup_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    gap: 12px;\n    padding-bottom: 30px;\n    width: 100%;\n"])));
var RadiosContainer = styled_components_browser_esm["b" /* default */].div(RadioGroup_styles_templateObject2 || (RadioGroup_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: ", ";\n    gap: 20px;\n"])), function (props) {
  return props.direction;
});
var RadioGroup_styles_Title = styled_components_browser_esm["b" /* default */].span(RadioGroup_styles_templateObject3 || (RadioGroup_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: normal;\n    letter-spacing: -0.32px;\n    text-align: center;\n    color: #2e2e2e;\n"])));
var styles_RadioInput = styled_components_browser_esm["b" /* default */].input(RadioGroup_styles_templateObject4 || (RadioGroup_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    margin-right: 10px;\n    width: 20px;\n    height: 20px;\n"])));
var RadioGroup_styles_Icon = styled_components_browser_esm["b" /* default */].img(RadioGroup_styles_templateObject5 || (RadioGroup_styles_templateObject5 = taggedTemplateLiteral_default()(["\n\n"])));
// CONCATENATED MODULE: ./src/components/RadioGroup/index.tsx





function RadioGroup(props) {
  var onChange = props.onChange,
    data = props.data,
    selectedValue = props.selectedValue,
    name = props.name,
    direction = props.direction,
    title = props.title;
  var getRadios = Object(react["useCallback"])(function () {
    return data.map(function (radioData) {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: onChange,
        name: name,
        value: radioData.value,
        selectedValue: selectedValue,
        title: radioData.title,
        disabled: radioData.disabled
      });
    });
  }, [data, selectedValue, onChange, name]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(RadioGroup_styles_Container, {
    children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(RadiosContainer, {
      direction: direction,
      children: getRadios()
    })]
  });
}
/* harmony default export */ var components_RadioGroup = (RadioGroup);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Target/styles.ts

var Target_styles_templateObject, Target_styles_templateObject2, Target_styles_templateObject3, Target_styles_templateObject4, Target_styles_templateObject5, Target_styles_templateObject6, Target_styles_templateObject7, Target_styles_templateObject8, Target_styles_templateObject9, Target_styles_templateObject10, Target_styles_templateObject11, Target_styles_templateObject12, Target_styles_templateObject13, Target_styles_templateObject14, Target_styles_templateObject15, styles_templateObject16, _templateObject17, _templateObject18, _templateObject19;

var Target_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject || (Target_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: center;\n"])));
var Target_styles_Container = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject2 || (Target_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n"])));
var EnvironmentContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject3 || (Target_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: ", ";\n    margin-top: 12px;\n"])), function (props) {
  return props.hide ? 'none' : 'block';
});
var TaskActionContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject4 || (Target_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    margin-top: 10px;\n"])));
var Target_styles_Actions = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject5 || (Target_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 15px;\n    flex-direction: column;\n    margin-top: 15px;\n"])));
var Target_styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject6 || (Target_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    margin-top: 15px;\n"])));
var Target_styles_Title = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject7 || (Target_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.25;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    padding-bottom: 15px;\n    border-bottom: ", ";\n"])), function (props) {
  return props.widthBorder ? '1px solid #ccc' : '';
});
var Section = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject8 || (Target_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    margin-top: 30px;\n    display: flex;\n    flex-direction: row;\n\n"])));
var SectionItemConatiner = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject9 || (Target_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 30px;\n    flex-direction: column;\n    width: 100%;\n    padding: 0px 21px;\n"])));
var ActionContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject10 || (Target_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    display: flex;\n    gap: 30px;\n\n"])));
var EntityCloneContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject11 || (Target_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    display: flex;\n    width: 100%;\n    gap: 15px;\n    height: 30px;\n    align-items: center;\n"])));
var Target_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span(Target_styles_templateObject12 || (Target_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    color: red;\n"])));
var CheckBoxContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject13 || (Target_styles_templateObject13 = taggedTemplateLiteral_default()(["\n    width: 100px;\n"])));
var SectionTitle = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject14 || (Target_styles_templateObject14 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n"])));
var RadioGroupContainer = styled_components_browser_esm["b" /* default */].div(Target_styles_templateObject15 || (Target_styles_templateObject15 = taggedTemplateLiteral_default()(["\n    border-bottom: 1px solid #ccc;\n"])));
var TestingEnvironmentContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject16 || (styles_templateObject16 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    margin-top: 13px;\n"])));
var styles_Leftside = styled_components_browser_esm["b" /* default */].div(_templateObject17 || (_templateObject17 = taggedTemplateLiteral_default()(["\n    width: 320px;\n    display: flex;\n    flex-direction: column;\n    gap: 20px;\n\n"])));
var Target_styles_RightSide = styled_components_browser_esm["b" /* default */].div(_templateObject18 || (_templateObject18 = taggedTemplateLiteral_default()(["\n    border-left: 2px solid #ccc;\n    padding-left: 35px;\n    flex-grow: 1;\n"])));
var EntityCloneComment = styled_components_browser_esm["b" /* default */].span(_templateObject19 || (_templateObject19 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: bold;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #666;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/Target/index.tsx



function Target_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Target_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Target_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Target_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }
















function TargetForm(props) {
  var _errors$num_of_entiti;
  var _useContext = Object(react["useContext"])(TaskContext),
    register = _useContext.register,
    clearErrors = _useContext.clearErrors,
    errors = _useContext.errors,
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    unregister = _useContext.unregister;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var be_name = taskData.be_name,
    environment_id = taskData.environment_id,
    load_entity = taskData.load_entity,
    delete_before_load = taskData.delete_before_load,
    reserve_ind = taskData.reserve_ind,
    replace_sequences = taskData.replace_sequences,
    reserve_retention_period_type = taskData.reserve_retention_period_type,
    reserve_retention_period_value = taskData.reserve_retention_period_value,
    reserve_note = taskData.reserve_note,
    clone_ind = taskData.clone_ind,
    num_of_clones = taskData.num_of_clones,
    target_env = taskData.target_env,
    reservationPeriodTypes = taskData.reservationPeriodTypes,
    maxReservationPeriod = taskData.maxReservationPeriod,
    be_id = taskData.be_id,
    dataSourceType = taskData.dataSourceType,
    selection_method = taskData.selection_method,
    version_ind = taskData.version_ind,
    source_type = taskData.source_type,
    synthetic_type = taskData.synthetic_type,
    generation_type = taskData.generation_type,
    maxToCopy = taskData.maxToCopy,
    userRole = taskData.userRole,
    deleteWarning = taskData.deleteWarning,
    source_environment_id = taskData.source_environment_id,
    reserve_only_task = taskData.reserve_only_task;
  var toast = hooks_useToast();
  var _useState = Object(react["useState"])(false),
    _useState2 = slicedToArray_default()(_useState, 2),
    disableAI = _useState2[0],
    setDisableAI = _useState2[1];
  Object(react["useEffect"])(function () {
    function fetchData() {
      return _fetchData.apply(this, arguments);
    }
    function _fetchData() {
      _fetchData = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data, ai_env;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getEnvironments(undefined, true);
            case 3:
              data = _context.sent;
              ai_env = data.find(function (it) {
                return it.synthetic_indicator === 'AI';
              });
              if (!ai_env || ai_env.permission.indexOf('write') < 0) {
                setDisableAI(true);
              }
              _context.next = 11;
              break;
            case 8:
              _context.prev = 8;
              _context.t0 = _context["catch"](0);
              setDisableAI(true);
            case 11:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 8]]);
      }));
      return _fetchData.apply(this, arguments);
    }
    if ((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) !== 'admin') {
      fetchData();
    }
    if (dataSourceType !== 'data_source' && dataSourceType !== undefined) {
      saveForm({
        delete_before_load: false,
        replace_sequences: true
      });
    }
  }, []);
  Object(react["useEffect"])(function () {
    if (deleteWarning === undefined) {
      saveForm({
        deleteWarning: false
      });
      return;
    }
    if (!delete_before_load && dataSourceType === 'data_source' && source_type === 'tables' && deleteWarning === false) {
      saveForm({
        deleteWarning: true
      });
      toast.warning('The load activity may cause data duplication or a violation of unique constraints');
    }
  }, [delete_before_load]);
  var targetEnvironmentsTypes = Object(react["useMemo"])(function () {
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      saveForm({
        target_env: 'target_env'
      });
      return [{
        value: 'target_env',
        title: 'Testing environment'
      }];
    }
    return [{
      value: 'target_env',
      title: 'Testing environment'
    }, {
      value: 'ai_training',
      title: 'AI training',
      disabled: disableAI
    }];
  }, [saveForm, dataSourceType, source_type, disableAI]);
  Object(react["useEffect"])(function () {
    var updateData = {};
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      updateData.load_entity = true;
      if (delete_before_load === undefined) {
        updateData.delete_before_load = true;
      }
      updateData.reserve_ind = false;
    }
    if (dataSourceType !== 'data_source' && synthetic_type === 'new_data' || dataSourceType === 'synthetic') {
      updateData.clone_ind = false;
    }
    saveForm(updateData);
  }, [dataSourceType, source_type, synthetic_type]);
  var targetEnvChange = Object(react["useCallback"])(function (item) {
    var updatedData = {
      environment_id: item && item.environment_id || undefined,
      environment_name: item && item.environment_name || undefined
    };
    if (!source_environment_id) {
      updatedData.mask_sensitive_data = item && item.mask_sensitive_data || false;
    }
    saveForm(updatedData);
  }, [saveForm]);
  Object(react["useEffect"])(function () {
    if (dataSourceType === 'ai_generated' || dataSourceType === 'synthetic') {
      saveForm({
        replace_sequences: true
      });
    }
  }, [dataSourceType]);
  Object(react["useEffect"])(function () {
    if (selection_method === 'ALL') {
      saveForm({
        clone_ind: false
      });
    }
  }, [selection_method]);
  Object(react["useEffect"])(function () {
    if (version_ind) {
      var updateData = {
        load_entity: true
      };
      if (deleteWarning === undefined) {
        updateData.delete_before_load = true;
      }
      saveForm(updateData);
    }
  }, [version_ind]);
  var actionChange = Object(react["useCallback"])(function (action, value) {
    saveForm(defineProperty_default()({}, action, value));
  }, [saveForm]);
  var replaceSequenceChange = Object(react["useCallback"])(function (value) {
    saveForm({
      replace_sequences: value || false
    });
  }, [saveForm]);
  var entityCloneChange = Object(react["useCallback"])(function (value) {
    saveForm({
      clone_ind: value || false,
      num_of_clones: undefined
    });
    clearErrors('num_of_clones');
  }, [saveForm, clearErrors]);
  var reserveNoteChange = Object(react["useCallback"])(function (value) {
    saveForm({
      reserve_note: value
    });
  }, [saveForm]);
  var numberOfCloneChange = Object(react["useCallback"])(function (value) {
    saveForm({
      num_of_clones: value
    });
  }, [saveForm]);
  var targetEnvTypeChange = Object(react["useCallback"])(function (value) {
    function fetchCheckAIInstaltion() {
      return _fetchCheckAIInstaltion.apply(this, arguments);
    }
    function _fetchCheckAIInstaltion() {
      _fetchCheckAIInstaltion = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
        return regenerator_default.a.wrap(function _callee2$(_context2) {
          while (1) switch (_context2.prev = _context2.next) {
            case 0:
              _context2.prev = 0;
              _context2.next = 3;
              return apis_task.checkAIInstallation('AITraining');
            case 3:
              _context2.next = 7;
              break;
            case 5:
              _context2.prev = 5;
              _context2.t0 = _context2["catch"](0);
            case 7:
            case "end":
              return _context2.stop();
          }
        }, _callee2, null, [[0, 5]]);
      }));
      return _fetchCheckAIInstaltion.apply(this, arguments);
    }
    if (value === 'ai_training') {
      fetchCheckAIInstaltion();
    }
    saveForm({
      target_env: value,
      environment_id: undefined,
      environment_name: undefined,
      load_entity: false,
      delete_before_load: false,
      reserve_ind: false,
      replace_sequences: false,
      entity_clone: false
    });
  }, [saveForm]);
  Object(react["useEffect"])(function () {
    if (!target_env) {
      saveForm({
        target_env: 'target_env'
      });
    }
  }, []);
  Object(react["useEffect"])(function () {
    if (!clone_ind) {
      unregister('num_of_clones');
    }
  }, [clone_ind]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Target_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(Target_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Target_styles_Title, {
        children: "Destination of test data"
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(RadioGroupContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RadioGroup, {
          title: '',
          data: targetEnvironmentsTypes,
          name: "target_env_types",
          selectedValue: target_env,
          onChange: targetEnvTypeChange
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TestingEnvironmentContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Leftside, {
          children: [!(dataSourceType === 'data_source' && source_type === 'tables') ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Target_styles_DataMovmentSettingsContainer, {
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_DataMovmentSettings, {
              enabledTabs: ['be'],
              type: 'target'
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), be_id ? /*#__PURE__*/Object(jsx_runtime["jsx"])(EnvironmentContainer, {
            hide: target_env !== 'target_env',
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_EnvironmentSelect, {
              title: 'Target environment',
              syntheticType: !target_env || target_env === 'target_env' ? 'None' : 'AI',
              be_name: be_name,
              environment_id: environment_id,
              onChange: targetEnvChange,
              mode: 'TARGET',
              isMandatory: true
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
        }), environment_id && target_env === 'target_env' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Target_styles_RightSide, {
          children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskActionContainer, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Target_styles_Title, {
              widthBorder: true,
              children: ["Actions to perform", /*#__PURE__*/Object(jsx_runtime["jsx"])(Target_styles_MadatoryAsterisk, {
                children: "*"
              })]
            }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Target_styles_Actions, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionContainer, {
                children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                  title: "Delete",
                  disabled: !(userRole !== null && userRole !== void 0 && userRole.allowed_delete_before_load) || dataSourceType === 'synthetic' || dataSourceType === 'ai_generated' || reserve_ind && !load_entity || (clone_ind || replace_sequences) && load_entity || version_ind && !(dataSourceType === 'data_source' && source_type === 'tables'),
                  onChange: function onChange(value) {
                    return actionChange('delete_before_load', value || false);
                  },
                  name: "delete_checkbox",
                  value: delete_before_load
                }), dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
                  children: "Delete the entire table data"
                }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
              }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionContainer, {
                children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CheckBoxContainer, {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                    disabled: reserve_only_task || delete_before_load && reserve_ind || dataSourceType === 'data_source' && source_type === 'tables',
                    title: "Load",
                    onChange: function onChange(value) {
                      return actionChange('load_entity', value || false);
                    },
                    name: "load_checkbox",
                    value: load_entity
                  })
                }), dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(SectionItemConatiner, {
                  children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                    disabled: !(userRole !== null && userRole !== void 0 && userRole.allowed_replace_sequences) || clone_ind || !load_entity || delete_before_load || dataSourceType === 'ai_generated' || dataSourceType === 'synthetic',
                    title: "Replace IDs for the copied entities",
                    onChange: replaceSequenceChange,
                    name: "replace_sequence_checkbox",
                    value: !delete_before_load && load_entity ? replace_sequences : false
                  }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(EntityCloneContainer, {
                    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                      disabled: !(userRole !== null && userRole !== void 0 && userRole.allowed_creation_of_synthetic_data) || !load_entity || delete_before_load || selection_method === 'ALL' || dataSourceType !== 'data_source' && synthetic_type === 'new_data' || dataSourceType !== 'data_source' && synthetic_type === 'generated_data' && generation_type === 'all',
                      title: "Generate clones of an entity",
                      onChange: entityCloneChange,
                      name: "clone_ind",
                      value: !delete_before_load && load_entity ? clone_ind : false
                    }), clone_ind && !delete_before_load && load_entity ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
                      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, Target_objectSpread(Target_objectSpread({}, register('num_of_clones', {
                        required: 'Populate number of clones',
                        min: {
                          value: 1,
                          message: 'Minimum Entites to clone is 1'
                        },
                        max: {
                          value: maxToCopy,
                          message: "Maximum Entites to clone is ".concat(maxToCopy)
                        }
                      })), {}, {
                        disabled: !load_entity || delete_before_load,
                        width: "160px",
                        name: "num_of_clones",
                        mandatory: true,
                        min: 1,
                        max: maxToCopy,
                        placeholder: 'Number of clones',
                        type: InputTypes.number,
                        value: num_of_clones,
                        onChange: numberOfCloneChange,
                        title: "",
                        error: (_errors$num_of_entiti = errors.num_of_entities) === null || _errors$num_of_entiti === void 0 ? void 0 : _errors$num_of_entiti.message
                      })), /*#__PURE__*/Object(jsx_runtime["jsx"])(EntityCloneComment, {
                        children: "The subset is limited to one entity"
                      })]
                    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
                  })]
                })]
              }), dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionContainer, {
                children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CheckBoxContainer, {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                    title: "Reserve",
                    disabled: delete_before_load && !load_entity || dataSourceType === 'data_source' && source_type === 'tables',
                    onChange: function onChange(value) {
                      return actionChange('reserve_ind', value || false);
                    },
                    name: "reserve_checkbox",
                    value: reserve_ind
                  })
                }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(SectionItemConatiner, {
                  children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Periods, {
                    disabled: !reserve_ind,
                    title: 'Reservation period',
                    reserve: true,
                    mandatory: reserve_ind,
                    periodsData: reservationPeriodTypes,
                    period_type: reserve_retention_period_type,
                    period_value: reserve_retention_period_value,
                    maxPeriod: maxReservationPeriod,
                    onChange: saveForm
                  }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
                    disabled: !reserve_ind,
                    width: "370px",
                    name: "reservation_note",
                    mandatory: false,
                    type: InputTypes.text,
                    value: reserve_note,
                    onChange: reserveNoteChange,
                    title: "Reservation note"
                  })]
                })]
              })]
            })]
          })
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })]
    })
  });
}
/* harmony default export */ var Target = (TargetForm);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/styles.ts

var Advanced_styles_templateObject, Advanced_styles_templateObject2, Advanced_styles_templateObject3, Advanced_styles_templateObject4, Advanced_styles_templateObject5, Advanced_styles_templateObject6, Advanced_styles_templateObject7, Advanced_styles_templateObject8;

var Advanced_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject || (Advanced_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: center;\n"])));
var Advanced_styles_Container = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject2 || (Advanced_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    position: relative;\n"])));
var styles_Section = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject3 || (Advanced_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    justify-content: space-between;\n    gap : 27px;\n    padding-bottom: 30px;\n    border-bottom: solid 1px #ccc;\n"])));
var TableWrapper = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject4 || (Advanced_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    margin-top: 30px;\n"])));
var ActionsColumn = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject5 || (Advanced_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    display:flex;\n    align-items: center;\n    gap: 7px;\n"])));
var ButtonContainer = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject6 || (Advanced_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    padding-bottom: 9px;\n    width: 100%;\n    display: flex;\n    justify-content: flex-end;\n"])));
var Advanced_styles_Icon = styled_components_browser_esm["b" /* default */].img(Advanced_styles_templateObject7 || (Advanced_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var styles_ResetButton = styled_components_browser_esm["b" /* default */].div(Advanced_styles_templateObject8 || (Advanced_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    z-index: 1;\n    position: absolute;\n    right: 0px;\n    top: -50px;\n    z-index: 100;\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1683f2;\n    display: flex;\n    gap: 6px;\n    align-items: center;\n    cursor: pointer;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/Scheduler/styles.ts

var Scheduler_styles_templateObject, Scheduler_styles_templateObject2, Scheduler_styles_templateObject3, Scheduler_styles_templateObject4, Scheduler_styles_templateObject5, Scheduler_styles_templateObject6, Scheduler_styles_templateObject7, Scheduler_styles_templateObject8;

var Scheduler_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject || (Scheduler_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: center;\n"])));
var Scheduler_styles_Container = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject2 || (Scheduler_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    flex-direction: column;\n    gap: 15px;\n"])));
var SchedulerTypes = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject3 || (Scheduler_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n    padding-bottom: 30px;\n    border-bottom: solid 1px #ccc;\n"])));
var EndByDate = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject4 || (Scheduler_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    align-items: center;\n    gap: 20px;\n    padding-bottom: 30px;\n    border-bottom: solid 1px #ccc;  \n"])));
var CronContainer = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject5 || (Scheduler_styles_templateObject5 = taggedTemplateLiteral_default()(["\n"])));
var EndByContainer = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject6 || (Scheduler_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    padding-top: 30px;\n"])));
var styles_DateContainer = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject7 || (Scheduler_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    padding-top: 30px;\n"])));
var TimeNote = styled_components_browser_esm["b" /* default */].div(Scheduler_styles_templateObject8 || (Scheduler_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    justify-content: flex-end;\n    color: #ec4758;\n    font-size: 14px;\n"])));
// EXTERNAL MODULE: ./node_modules/angular/index.js
var node_modules_angular = __webpack_require__(66);
var angular_default = /*#__PURE__*/__webpack_require__.n(node_modules_angular);

// CONCATENATED MODULE: ./src/components/AngularJSWrapper/index.tsx




function AngularJSWrapper(props) {
  var comp = props.comp,
    params = props.params,
    save = props.save;
  var containerRef = Object(react["useRef"])(null);
  var _useState = Object(react["useState"])({}),
    _useState2 = slicedToArray_default()(_useState, 2),
    scope = _useState2[0],
    setScope = _useState2[1];
  Object(react["useEffect"])(function () {
    Object.keys(params).forEach(function (it) {
      scope[params[it].name] = params[it].value;
    });
  }, [params, scope]);
  Object(react["useEffect"])(function () {
    if (containerRef.current) {
      // Manually bootstrap the AngularJS application

      // Compile and inject the AngularJS component
      var $injector = angular_default.a.element(containerRef.current).injector();
      var $compile = $injector.get('$compile');
      var $rootScope = $injector.get('$rootScope');
      var newScope = $rootScope.$new();
      Object.keys(params).forEach(function (key) {
        newScope[params[key].name] = params[key].value;
      });
      var element = angular_default.a.element("<".concat(comp, " ").concat(getParams(), "></").concat(comp, ">"));
      var compiledElement = $compile(element)(newScope);
      containerRef.current.appendChild(compiledElement[0]);
      newScope.$apply();
      setScope(newScope);
      newScope.$watch(params['ng-model'].name, function (newValue, value) {
        save(params['ng-model'].name, newValue);
      }, true, true);
    }
    // Cleanup on component unmount
    return function () {
      // Remove the AngularJS component and clean up the scope
      if (containerRef.current) {
        var _scope = angular_default.a.element(containerRef.current).scope();
        _scope.$destroy();
      }
    };
  }, []);
  var getParams = Object(react["useCallback"])(function () {
    var attributes = Object.keys(params).map(function (it) {
      return " ".concat(it, "=\"").concat(params[it].name, "\" ");
    }).join(' ');
    console.log(attributes);
    return attributes;
  }, [params]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
    ref: containerRef
  });
}
/* harmony default export */ var components_AngularJSWrapper = (AngularJSWrapper);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Scheduler/index.tsx












var ScheduleTypesEnum = /*#__PURE__*/function (ScheduleTypesEnum) {
  ScheduleTypesEnum["EXECUTION_BY_REQUEST"] = "EXECUTION_BY_REQUEST";
  ScheduleTypesEnum["SCHEDULED_EXECUTION"] = "SCHEDULED_EXECUTION";
  return ScheduleTypesEnum;
}(ScheduleTypesEnum || {});
function SchedulerForm(props) {
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var scheduler = taskData.scheduler,
    scheduling_end_date = taskData.scheduling_end_date;
  var _useState = Object(react["useState"])(scheduler || ''),
    _useState2 = slicedToArray_default()(_useState, 2),
    cronValue = _useState2[0],
    setCronValue = _useState2[1];
  var _useState3 = Object(react["useState"])(scheduling_end_date && new Date(scheduling_end_date) || null),
    _useState4 = slicedToArray_default()(_useState3, 2),
    cronEndDate = _useState4[0],
    setCornEndDate = _useState4[1];
  var _useState5 = Object(react["useState"])(false),
    _useState6 = slicedToArray_default()(_useState5, 2),
    schedulingParameters = _useState6[0],
    setSchedulingParameters = _useState6[1];
  var _useState7 = Object(react["useState"])(scheduling_end_date ? 'end_by' : 'none'),
    _useState8 = slicedToArray_default()(_useState7, 2),
    endBy = _useState8[0],
    setEndBy = _useState8[1];
  Object(react["useEffect"])(function () {
    setSchedulingParameters(scheduler !== 'immediate');
  }, [scheduler]);
  var schedulingParametersOnChange = Object(react["useCallback"])(function (value) {
    if (value) {
      saveForm({
        scheduler: '0 0/1 * 1/1 * ? *'
      });
    } else {
      saveForm({
        scheduler: 'immediate'
      });
    }
  }, [saveForm, setSchedulingParameters]);
  var cronChange = Object(react["useCallback"])(function (value) {
    setCronValue(value);
    saveForm({
      scheduler: value
    });
  }, [saveForm]);
  var endByChange = Object(react["useCallback"])(function (value) {
    setEndBy(value);
    saveForm({
      scheduling_end_date: value === 'none' ? null : cronEndDate
    });
  }, [saveForm, cronEndDate]);
  var cronEndDateChange = Object(react["useCallback"])(function (value) {
    setCornEndDate(value);
    saveForm({
      scheduling_end_date: value && value.toDateString() || null
    });
  }, [saveForm]);
  var translateFn = function translateFn(key) {
    if (key === 'hour') {
      return 'hour(s)';
    }
    return key;
  };
  var params = Object(react["useMemo"])(function () {
    return {
      'ng-model': {
        name: 'scheduler',
        value: scheduler
      },
      options: {
        name: 'options',
        value: {
          formInputClass: 'form-control1 cron-gen-input',
          // Form input class override
          formSelectClass: 'form-control1 cron-gen-select',
          // Select class override
          formRadioClass: 'cron-gen-radio',
          // Radio class override
          formCheckboxClass: 'cron-gen-checkbox',
          // Radio class override
          hideMinutesTab: false,
          // Whether to hide the minutes tab
          hideHourlyTab: false,
          // Whether to hide the hourly tab
          hideDailyTab: false,
          // Whether to hide the daily tab
          hideWeeklyTab: false,
          // Whether to hide the weekly tab
          hideMonthlyTab: false,
          // Whether to hide the monthly tab
          hideYearlyTab: false,
          // Whether to hide the yearly tab
          hideAdvancedTab: false,
          // Whether to hide the advanced tab
          use24HourTime: true,
          // Whether to show AM/PM on the time selectors
          hideSeconds: false // Whether to show/hide the seconds time picker
        }
      },
      'cron-format': {
        name: 'format',
        value: "quartz (Currently only compatible with 'quartz' and defaults to 'quartz')"
      }
    };
  }, [scheduler]);
  var saveSchedulerValue = Object(react["useCallback"])(function (field, value) {
    saveForm(defineProperty_default()({}, field, value));
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Scheduler_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(Scheduler_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
        name: 'set_scheduling_parameters',
        title: 'Set scheduling parameters',
        onChange: function onChange(value) {
          return schedulingParametersOnChange(value);
        },
        value: schedulingParameters
      }), schedulingParameters ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(CronContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_AngularJSWrapper, {
          comp: 'cron-gen',
          params: params,
          save: saveSchedulerValue
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TimeNote, {
          children: "Task execution time will be based on UTC time zone"
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(EndByContainer, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(EndByDate, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
              onChange: endByChange,
              name: "end_by_date",
              value: 'end_by',
              selectedValue: endBy,
              title: 'End by date'
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
              onChange: endByChange,
              name: "end_by_date",
              value: 'none',
              selectedValue: endBy,
              title: 'No end date'
            })]
          }), endBy === 'end_by' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_DateContainer, {
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(DatePicker, {
              onChange: cronEndDateChange,
              date: cronEndDate,
              minDate: new Date()
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
        })]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    })
  });
}
/* harmony default export */ var Scheduler = (SchedulerForm);
// EXTERNAL MODULE: ./node_modules/@babel/runtime/helpers/objectDestructuringEmpty.js
var objectDestructuringEmpty = __webpack_require__(225);
var objectDestructuringEmpty_default = /*#__PURE__*/__webpack_require__.n(objectDestructuringEmpty);

// CONCATENATED MODULE: ./src/images/delete-icon-blue.svg
/* harmony default export */ var delete_icon_blue = ("js/dist/437f49031698b544e4b1c9c124aaf20a.svg");
// CONCATENATED MODULE: ./src/images/edit.svg
/* harmony default export */ var edit = ("js/dist/ad51b02c0cfd4e1a26763e5d4e87b925.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariables/useTable.tsx







var TaskVariables_useTable_useTable = function useTable(deleteGlobal, editGloabl) {
  var columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  var columns = Object(react["useMemo"])(function () {
    return [{
      id: 'actions',
      header: '',
      cell: function cell(_ref) {
        var row = _ref.row;
        return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionsColumn, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Icon, {
            onClick: function onClick() {
              return editGloabl(row.original.global_name);
            },
            src: edit
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Icon, {
            onClick: function onClick() {
              return deleteGlobal(row.original.global_name);
            },
            src: delete_icon_blue
          })]
        });
      }
    }, columnHelper.accessor('global_name', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Variable name"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('global_value', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Variable value"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    }), columnHelper.accessor('lu_name', {
      header: function header() {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: "Logical unit"
        });
      },
      cell: function cell(info) {
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
          children: info.getValue()
        });
      }
    })];
  }, [editGloabl, deleteGlobal, columnHelper]);
  return {
    columns: columns
  };
};
/* harmony default export */ var TaskVariables_useTable = (TaskVariables_useTable_useTable);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariables/styles.ts

var TaskVariables_styles_templateObject;

var styles_ButtonContainer = styled_components_browser_esm["b" /* default */].div(TaskVariables_styles_templateObject || (TaskVariables_styles_templateObject = taggedTemplateLiteral_default()(["\n    padding-bottom: 9px;\n    position: relative;\n    display: flex;\n    justify-content: flex-end;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariablesModal/styles.ts

var TaskVariablesModal_styles_templateObject, TaskVariablesModal_styles_templateObject2, TaskVariablesModal_styles_templateObject3, TaskVariablesModal_styles_templateObject4, TaskVariablesModal_styles_templateObject5, TaskVariablesModal_styles_templateObject6, TaskVariablesModal_styles_templateObject7, TaskVariablesModal_styles_templateObject8, TaskVariablesModal_styles_templateObject9, TaskVariablesModal_styles_templateObject10;

var TaskVariablesModal_styles_Container = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject || (TaskVariablesModal_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 324px;\n    position: relative;\n    z-index: 100;\n    overflow-y: auto;\n    overflow-x: hidden;\n    padding: 19px 0px 30px 0px;\n    object-fit: contain;\n    border-radius: 6px;\n    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);\n    background-color: #fff;\n"])));
var TaskVariablesModal_styles_Title = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject2 || (TaskVariablesModal_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 18px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.33;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    position: relative;\n    margin: 0px 20px;\n    margin-bottom: 19px;\n"])));
var TaskVariablesModal_styles_Body = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject3 || (TaskVariablesModal_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    margin: 24px 25px 0px 30px;\n"])));
var ItemsContainer = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject4 || (TaskVariablesModal_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    align-items: flex-start;\n    gap: 15px;\n\n"])));
var TaskVariablesModal_styles_Seprator = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject5 || (TaskVariablesModal_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    border: solid 1px #ccc;\n"])));
var styles_CloseIcon = styled_components_browser_esm["b" /* default */].img(TaskVariablesModal_styles_templateObject6 || (TaskVariablesModal_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    right: 0px;\n    top: 5px;\n    cursor: pointer;\n"])));
var TaskVariablesModal_styles_Icon = styled_components_browser_esm["b" /* default */].img(TaskVariablesModal_styles_templateObject7 || (TaskVariablesModal_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var TaskVariablesModal_styles_Actions = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject8 || (TaskVariablesModal_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    display: flex;\n    margin-top:5px;\n    align-items: center;\n    justify-content: flex-end;\n    gap: 18px;\n    border-bottom: ", ";\n    padding-bottom: 13px;\n"])), function (props) {
  return props.border ? '1px solid #ccc' : '';
});
var TaskVariablesModal_styles_ActionItem = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject9 || (TaskVariablesModal_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    cursor: pointer;\n"])));
var styles_ErrorContainer = styled_components_browser_esm["b" /* default */].div(TaskVariablesModal_styles_templateObject10 || (TaskVariablesModal_styles_templateObject10 = taggedTemplateLiteral_default()(["\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariablesModal/index.tsx




function TaskVariablesModal_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function TaskVariablesModal_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? TaskVariablesModal_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : TaskVariablesModal_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }









function TaskVariablesModal(props) {
  var setOpen = props.setOpen,
    variableData = props.variableData,
    selectedVariables = props.selectedVariables,
    lus = props.lus,
    addGlobal = props.addGlobal;
  var _useState = Object(react["useState"])(),
    _useState2 = slicedToArray_default()(_useState, 2),
    chosenVariable = _useState2[0],
    setChosenVariable = _useState2[1];
  var _useState3 = Object(react["useState"])(),
    _useState4 = slicedToArray_default()(_useState3, 2),
    localVariablesData = _useState4[0],
    setLocalVariablesData = _useState4[1];
  var _useState5 = Object(react["useState"])(null),
    _useState6 = slicedToArray_default()(_useState5, 2),
    chosenLu = _useState6[0],
    setChosenLu = _useState6[1];
  var _useState7 = Object(react["useState"])([]),
    _useState8 = slicedToArray_default()(_useState7, 2),
    variableLUList = _useState8[0],
    setVariableLUList = _useState8[1];
  var _useState9 = Object(react["useState"])(false),
    _useState10 = slicedToArray_default()(_useState9, 2),
    showError = _useState10[0],
    setShowError = _useState10[1];
  var _useState11 = Object(react["useState"])(),
    _useState12 = slicedToArray_default()(_useState11, 2),
    variableValue = _useState12[0],
    setVariableValue = _useState12[1];
  Object(react["useEffect"])(function () {
    function fetchTaskVariables() {
      return _fetchTaskVariables.apply(this, arguments);
    }
    function _fetchTaskVariables() {
      _fetchTaskVariables = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var _data, globals, found, foundLU;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getGlobalVariables(lus);
            case 3:
              _data = _context.sent;
              globals = [];
              _data.forEach(function (global) {
                var luObjectMapping = {};
                var selectedLuForGlobal = selectedVariables.filter(function (it) {
                  return it.global_name === global.globalName;
                });
                if (selectedLuForGlobal.find(function (it) {
                  return it.lu_name === 'ALL';
                }) && (!variableData || variableData.global_name !== global.globalName)) {
                  return;
                }
                global.luList.forEach(function (luData) {
                  luObjectMapping[luData.luName] = luData.defaultValue;
                });
                if (luObjectMapping['ALL'] !== undefined && luObjectMapping['ALL'] !== null) {
                  var newLuList = [];
                  if (selectedLuForGlobal.length > 0) {}
                  if (selectedLuForGlobal.length === 0) {
                    newLuList.push({
                      luName: 'ALL',
                      defaultValue: luObjectMapping['ALL'],
                      value: 'ALL',
                      label: 'ALL'
                    });
                  }
                  lus.forEach(function (lu) {
                    newLuList.push({
                      luName: lu,
                      defaultValue: luObjectMapping[lu] || luObjectMapping['ALL'] || '',
                      value: lu,
                      label: lu
                    });
                  });
                  global.luList = newLuList;
                }
                global.luList = global.luList.filter(function (lu) {
                  return selectedLuForGlobal.findIndex(function (it) {
                    return it.lu_name === lu.luName;
                  }) < 0 || lu.luName === (variableData === null || variableData === void 0 ? void 0 : variableData.lu_name);
                });
                if (global.luList.length === 0 && (!variableData || variableData.global_name !== global.globalName)) {
                  return;
                }
                if (variableData && !global.luList.find(function (it) {
                  return it.luName === (variableData === null || variableData === void 0 ? void 0 : variableData.lu_name);
                })) {
                  global.luList.push({
                    luName: variableData.lu_name,
                    defaultValue: variableData.global_value,
                    value: variableData.lu_name,
                    label: variableData.lu_name
                  });
                }
                globals.push(TaskVariablesModal_objectSpread(TaskVariablesModal_objectSpread({}, global), {}, {
                  value: global.globalName,
                  label: global.globalName
                }));
              });
              if (variableData) {
                found = globals.find(function (it) {
                  return it.globalName === variableData.global_name;
                });
                if (found) {
                  setChosenVariable(found);
                  setVariableLUList(found.luList.map(function (it) {
                    return TaskVariablesModal_objectSpread(TaskVariablesModal_objectSpread({}, it), {}, {
                      value: it.luName,
                      label: it.luName
                    });
                  }));
                  foundLU = found.luList.find(function (it) {
                    return it.luName === variableData.lu_name;
                  });
                  if (foundLU) {
                    setChosenLu(foundLU);
                    setVariableValue(variableData.global_value);
                  }
                }
              }
              setLocalVariablesData(globals);
              _context.next = 12;
              break;
            case 10:
              _context.prev = 10;
              _context.t0 = _context["catch"](0);
            case 12:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 10]]);
      }));
      return _fetchTaskVariables.apply(this, arguments);
    }
    fetchTaskVariables();
  }, []);
  console.log(localVariablesData);
  var variableChange = Object(react["useCallback"])(function (value) {
    setShowError(false);
    setChosenVariable(value);
    setChosenLu(null);
    setVariableValue('');
    setVariableLUList(value.luList.map(function (it) {
      return TaskVariablesModal_objectSpread(TaskVariablesModal_objectSpread({}, it), {}, {
        value: it.luName,
        label: it.luName
      });
    }));
  }, [setChosenVariable, setVariableLUList, setChosenLu, setVariableValue, setShowError]);
  var luChange = Object(react["useCallback"])(function (value) {
    setShowError(false);
    setChosenLu(value);
    if (value.defaultValue) {
      setVariableValue(value.defaultValue);
    } else {
      setVariableValue('');
    }
  }, [setChosenLu, setVariableValue, setShowError]);
  var varaiableValueChange = Object(react["useCallback"])(function (value) {
    setShowError(false);
    setVariableValue(value);
  }, [setVariableValue, setShowError]);
  var saveGlobal = Object(react["useCallback"])(function () {
    if (!chosenVariable || !chosenLu || !variableValue) {
      setShowError(true);
      return;
    }
    addGlobal({
      global_name: chosenVariable.value,
      global_value: variableValue,
      lu_name: chosenLu.value,
      edit: variableData ? true : false
    });
    setOpen(false);
  }, [setShowError, chosenVariable, chosenLu, variableValue, addGlobal, setOpen, variableData]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskVariablesModal_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskVariablesModal_styles_Title, {
      children: ["Task variables", /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_CloseIcon, {
        onClick: function onClick() {
          return setOpen(false);
        },
        src: xclose
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskVariablesModal_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskVariablesModal_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(ItemsContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
          width: "100%",
          title: 'Variable name',
          mandatory: true,
          value: chosenVariable,
          options: localVariablesData,
          loading: false,
          onChange: variableChange,
          disabled: variableData ? true : false
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
          width: "100%",
          title: 'Logical unit',
          mandatory: true,
          value: chosenLu,
          options: variableLUList,
          loading: false,
          onChange: luChange
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
          name: "variable_value",
          title: 'Variable value',
          mandatory: true,
          value: variableValue,
          onChange: varaiableValueChange,
          type: InputTypes.text,
          placeholder: ""
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_ErrorContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
          relativePosition: true,
          submit: showError,
          error: 'Mandatory fields are required'
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskVariablesModal_styles_Actions, {
        border: false,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskVariablesModal_styles_ActionItem, {
          onClick: function onClick() {
            return setOpen(false);
          },
          children: "Cancel"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskVariablesModal_styles_ActionItem, {
          onClick: saveGlobal,
          children: "Save"
        })]
      })]
    })]
  });
}
/* harmony default export */ var Advanced_TaskVariablesModal = (TaskVariablesModal);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariables/index.tsx




function TaskVariables_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function TaskVariables_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? TaskVariables_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : TaskVariables_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }











function TaskVariables(props) {
  objectDestructuringEmpty_default()(props);
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var _useState = Object(react["useState"])(undefined),
    _useState2 = slicedToArray_default()(_useState, 2),
    editVariableData = _useState2[0],
    setEditVariableData = _useState2[1];
  var _useState3 = Object(react["useState"])(false),
    _useState4 = slicedToArray_default()(_useState3, 2),
    open = _useState4[0],
    setOpen = _useState4[1];
  var ref = Object(react["useRef"])();
  var task_id = taskData.task_id,
    selected_logical_units_names = taskData.selected_logical_units_names,
    globals = taskData.globals;
  var editGloabl = Object(react["useCallback"])(function (globalName) {
    console.log(globals);
    var found = globals.find(function (it) {
      return it.global_name === globalName;
    });
    if (found) {
      setEditVariableData(TaskVariables_objectSpread({}, found));
      setOpen(true);
    }
  }, [globals, setEditVariableData]);
  console.log(globals);
  var deleteGlobal = Object(react["useCallback"])(function (globalName) {
    var newGlobals = globals.filter(function (it) {
      return it.global_name !== globalName;
    });
    saveForm({
      globals: newGlobals
    });
  }, [saveForm, globals]);
  var _useTable = TaskVariables_useTable(deleteGlobal, editGloabl),
    columns = _useTable.columns;
  var addNewGloabl = Object(react["useCallback"])(function (data) {
    if (data.edit) {
      var foundGlobal = globals.find(function (it) {
        return it.global_name === data.global_name;
      });
      if (foundGlobal) {
        foundGlobal.lu_name = data.lu_name;
        foundGlobal.global_value = data.global_value;
      }
      saveForm({
        globals: toConsumableArray_default()(globals)
      });
      setEditVariableData(undefined);
      return;
    }
    var newGlobals = [].concat(toConsumableArray_default()(globals), [data]);
    saveForm({
      globals: toConsumableArray_default()(newGlobals)
    });
  }, [globals, saveForm]);
  var getTaskVariablesModal = Object(react["useCallback"])(function () {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_TaskVariablesModal, {
      addGlobal: addNewGloabl,
      selectedVariables: globals,
      setOpen: setOpen,
      variableData: editVariableData,
      lus: selected_logical_units_names || []
    });
  }, [setOpen, addNewGloabl, globals, selected_logical_units_names, editVariableData]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_ButtonContainer, {
      ref: ref,
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
        containerStyle: {
          zIndex: '100'
        },
        reposition: false,
        padding: 10,
        align: "center",
        isOpen: open,
        positions: ['left'],
        content: getTaskVariablesModal(),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          onClick: function onClick() {
            return setOpen(true);
          },
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
            width: "152px",
            type: 'secondary',
            title: "Set task variables",
            onClick: function onClick() {}
          })
        })
      })
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Table, {
      columns: columns,
      data: globals
    })]
  });
}
/* harmony default export */ var Advanced_TaskVariables = (TaskVariables);
// CONCATENATED MODULE: ./src/images/clock-icon.svg
/* harmony default export */ var clock_icon = ("js/dist/b8c44964b9549d7786e9a44faffd0d7e.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/ExecutionPorcesses/styles.ts

var ExecutionPorcesses_styles_templateObject, ExecutionPorcesses_styles_templateObject2, ExecutionPorcesses_styles_templateObject3, ExecutionPorcesses_styles_templateObject4, ExecutionPorcesses_styles_templateObject5, ExecutionPorcesses_styles_templateObject6, ExecutionPorcesses_styles_templateObject7, ExecutionPorcesses_styles_templateObject8, ExecutionPorcesses_styles_templateObject9, ExecutionPorcesses_styles_templateObject10, ExecutionPorcesses_styles_templateObject11, ExecutionPorcesses_styles_templateObject12, ExecutionPorcesses_styles_templateObject13, ExecutionPorcesses_styles_templateObject14, ExecutionPorcesses_styles_templateObject15, ExecutionPorcesses_styles_templateObject16, styles_templateObject17, styles_templateObject18, styles_templateObject19, _templateObject20, _templateObject21, _templateObject22, _templateObject23, _templateObject24, _templateObject25, _templateObject26, _templateObject27, _templateObject28;

var ExecutionPorcesses_styles_Container = styled_components_browser_esm["b" /* default */].div(ExecutionPorcesses_styles_templateObject || (ExecutionPorcesses_styles_templateObject = taggedTemplateLiteral_default()(["\n    display: flex;\n    flex-direction: column;\n    gap: 15px;\n"])));
var AddButtonContainer = styled_components_browser_esm["b" /* default */].div(ExecutionPorcesses_styles_templateObject2 || (ExecutionPorcesses_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    display: flex;\n    align-items: center;\n    justify-content: flex-end;\n"])));
var ExecutionPorcesses_styles_TableContainer = styled_components_browser_esm["b" /* default */].table(ExecutionPorcesses_styles_templateObject3 || (ExecutionPorcesses_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    border-spacing: 0;\n    border-collapse: separate;\n    width: 100%;\n    height: 100%;\n"])));
var ExecutionPorcesses_styles_Thead = styled_components_browser_esm["b" /* default */].thead(ExecutionPorcesses_styles_templateObject4 || (ExecutionPorcesses_styles_templateObject4 = taggedTemplateLiteral_default()(["\n    display: table;\n    width: calc(100% - 5px);\n    table-layout: fixed;\n"])));
var TheadRow = styled_components_browser_esm["b" /* default */].tr(ExecutionPorcesses_styles_templateObject5 || (ExecutionPorcesses_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    vertical-align: top;\n"])));
var TheadEmptyColumn = styled_components_browser_esm["b" /* default */].th(ExecutionPorcesses_styles_templateObject6 || (ExecutionPorcesses_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    text-align: start;\n    font-size: 14px;\n    padding: 4px 6px;\n    vertical-align: middle;\n    width: 45px;\n    background-color: white;\n    border: 0;\n    color: black;\n"])));
var TheadNameColumn = styled_components_browser_esm["b" /* default */].th(ExecutionPorcesses_styles_templateObject7 || (ExecutionPorcesses_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    overflow: visible;\n    white-space: nowrap;\n    text-align: start;\n    font-size: 14px;\n    padding: 4px 6px;\n    vertical-align: middle;\n    background-color: white;\n    border: 0;\n    color: black;\n"])));
var TheadOrderColumn = styled_components_browser_esm["b" /* default */].th(ExecutionPorcesses_styles_templateObject8 || (ExecutionPorcesses_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    width: 20%;\n    padding-left: 8px;\n    text-align: center;\n    text-align: start;\n    font-size: 14px;\n    padding: 4px 6px;\n    vertical-align: middle;\n    background-color: white;\n    border: 0;\n    color: black;\n"])));
var TheadDeleteColumn = styled_components_browser_esm["b" /* default */].th(ExecutionPorcesses_styles_templateObject9 || (ExecutionPorcesses_styles_templateObject9 = taggedTemplateLiteral_default()(["\n    width: 40px;\n    text-align: center;\n    text-align: start;\n    font-size: 14px;\n    padding: 4px 6px;\n    vertical-align: middle;\n    background-color: white;\n    border: 0;\n"])));
var TBody = styled_components_browser_esm["b" /* default */].tbody(ExecutionPorcesses_styles_templateObject10 || (ExecutionPorcesses_styles_templateObject10 = taggedTemplateLiteral_default()(["\n    display: block;\n    overflow-x: hidden;\n    border-spacing: 0;\n    overflow: auto;\n"])));
var TBodyRow = styled_components_browser_esm["b" /* default */].tr(ExecutionPorcesses_styles_templateObject11 || (ExecutionPorcesses_styles_templateObject11 = taggedTemplateLiteral_default()(["\n    display: table;\n    width: calc(100% - 5px);\n    min-height: 42px;\n    table-layout: fixed;\n    vertical-align: top;\n    margin: 6px 0;\n    margin-bottom: ", ";\n    &:hover {\n        background-color: transparent;\n    }\n"])), function (props) {
  return props.expand ? '0px' : '';
});
var TBodyEditColumn = styled_components_browser_esm["b" /* default */].td(ExecutionPorcesses_styles_templateObject12 || (ExecutionPorcesses_styles_templateObject12 = taggedTemplateLiteral_default()(["\n    border: unset;\n    padding: unset;\n    white-space: unset;\n    max-width: unset;\n    overflow: unset;\n\n    text-align: start;\n    padding: 4px 6px;\n    vertical-align: middle;\n    width: 45px;\n    border: 1px solid #cccccc;\n    border-left: none;\n    border: none;\n    border-top: 1px solid #ccc;\n    border-bottom: 1px solid #ccc;\n    border-top-left-radius: 8px;\n    border-bottom-left-radius: ", ";\n    border-left: 1px solid #cccccc;\n"])), function (props) {
  return props.expand ? '0px' : '8px';
});
var TBodyNameColumn = styled_components_browser_esm["b" /* default */].td(ExecutionPorcesses_styles_templateObject13 || (ExecutionPorcesses_styles_templateObject13 = taggedTemplateLiteral_default()(["\n    text-align: start;\n    padding: 4px 6px;\n    vertical-align: middle;\n    border: 1px solid #cccccc;\n    border-left: none;\n    border: none;\n    border-top: 1px solid #ccc;\n    border-bottom: 1px solid #ccc;\n    color: ", ";\n"])), function (props) {
  return props.expand ? 'rgb(20, 131, 243);' : '';
});
var TBodyOrderColumn = styled_components_browser_esm["b" /* default */].td(ExecutionPorcesses_styles_templateObject14 || (ExecutionPorcesses_styles_templateObject14 = taggedTemplateLiteral_default()(["\n    text-align: start;\n    padding: 4px 6px;\n    vertical-align: middle;\n    border: 1px solid #cccccc;\n    border-left: none;\n    border: none;\n    border-top: 1px solid #ccc;\n    border-bottom: 1px solid #ccc;\n    width: 20%;\n    max-width: 20%;\n    overflow: visible;\n    white-space: nowrap;\n"])));
var TBodyDeleteColumn = styled_components_browser_esm["b" /* default */].td(ExecutionPorcesses_styles_templateObject15 || (ExecutionPorcesses_styles_templateObject15 = taggedTemplateLiteral_default()(["\n    text-align: start;\n    padding: 4px 6px;\n    vertical-align: middle;\n    border: 1px solid #cccccc;\n    border-left: none;\n    border: none;\n    border-top: 1px solid #ccc;\n    border-bottom: 1px solid #ccc;\n    width: 40px;\n    text-align: center;\n    border-top-right-radius: 8px;\n    border-bottom-right-radius: ", ";\n    border-right: 1px solid #cccccc;\n"])), function (props) {
  return props.expand ? '0px' : '8px';
});
var ExecutionPorcesses_styles_Icon = styled_components_browser_esm["b" /* default */].img(ExecutionPorcesses_styles_templateObject16 || (ExecutionPorcesses_styles_templateObject16 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n"])));
var EditIconContainer = styled_components_browser_esm["b" /* default */].div(styles_templateObject17 || (styles_templateObject17 = taggedTemplateLiteral_default()(["\n    border: 1px solid  ", ";\n    background: ", ";\n    color: ", ";\n    border-radius: 50%;\n    display: flex;\n    justify-content: center;\n    align-items: center;\n    width: 22px;\n    height: 22px;\n    padding: 2px;\n    font-size: 16px;\n    cursor: pointer;\n    transition: all .5s ease;\n    margin: 5px;\n    user-select: none;\n"])), function (props) {
  return props.expand ? '#0c84f3' : '#2e2e2e';
}, function (props) {
  return props.expand ? '#0c84f3' : '';
}, function (props) {
  return props.expand ? '#fff' : '#2e2e2e';
});
var EditIcon = styled_components_browser_esm["b" /* default */].div(styles_templateObject18 || (styles_templateObject18 = taggedTemplateLiteral_default()(["\n    padding: 0px 0px 0px 0px;\n    user-select: none;\n    color: ", ";\n"])), function (props) {
  return props.mandatory ? 'red' : props.edited ? '#1683f2' : '';
});
var TBodyExpandRow = styled_components_browser_esm["b" /* default */].tr(styles_templateObject19 || (styles_templateObject19 = taggedTemplateLiteral_default()(["\n    padding-top: 0 !important;\n    border: 1px solid #cccccc !important;\n    border-top: none !important;\n    width: calc(100% - 5px) !important;\n    margin-bottom: 6px;\n    border-bottom-left-radius: 8px;\n    border-bottom-right-radius: 8px;\n    vertical-align: top;\n    margin: 0px 0px 6px 0px;\n    display: table;\n    table-layout: fixed;\n    background-color: white !important;\n"])));
var TBodyExpandContainer = styled_components_browser_esm["b" /* default */].td(_templateObject20 || (_templateObject20 = taggedTemplateLiteral_default()(["\n    text-align: start;\n    display: flex;\n    border: 1px solid #cccccc;\n    padding-bottom: 10px !important;\n    border: none !important;\n    padding: 4px 6px;\n    vertical-align: middle;\n    border: 1px solid #cccccc;\n    border-left: none;\n    border: none;\n    border-top: 1px solid #ccc;\n    border-bottom: 1px solid #ccc;\n    max-width: 100%;\n    width: 100%;\n"])));
var TBodyExpandContent = styled_components_browser_esm["b" /* default */].div(_templateObject21 || (_templateObject21 = taggedTemplateLiteral_default()(["\n    user-select: none;\n    width: 50%;\n    margin: 0 35px;\n"])));
var ModalAddContainer = styled_components_browser_esm["b" /* default */].div(_templateObject22 || (_templateObject22 = taggedTemplateLiteral_default()(["\n    width: 400px;\n    min-height: 300px;\n    position: relative;\n    z-index: 100;\n    overflow-y: auto;\n    overflow-x: hidden;\n    padding: 19px 0px 30px 0px;\n    object-fit: contain;\n    border-radius: 6px;\n    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);\n    background-color: #fff;\n"])));
var ExecutionPorcesses_styles_Title = styled_components_browser_esm["b" /* default */].div(_templateObject23 || (_templateObject23 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 18px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.33;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    position: relative;\n    margin: 0px 20px;\n    margin-bottom: 19px;\n"])));
var ExecutionPorcesses_styles_Body = styled_components_browser_esm["b" /* default */].div(_templateObject24 || (_templateObject24 = taggedTemplateLiteral_default()(["\n    margin: 24px 25px 0px 30px;\n"])));
var ExecutionPorcesses_styles_Seprator = styled_components_browser_esm["b" /* default */].div(_templateObject25 || (_templateObject25 = taggedTemplateLiteral_default()(["\n    border: solid 1px #ccc;\n"])));
var ExecutionPorcesses_styles_CloseIcon = styled_components_browser_esm["b" /* default */].img(_templateObject26 || (_templateObject26 = taggedTemplateLiteral_default()(["\n    position: absolute;\n    right: 0px;\n    top: 5px;\n    cursor: pointer;\n"])));
var ExecutionPorcesses_styles_Actions = styled_components_browser_esm["b" /* default */].div(_templateObject27 || (_templateObject27 = taggedTemplateLiteral_default()(["\n    display: flex;\n    margin-top:5px;\n    align-items: center;\n    justify-content: flex-end;\n    gap: 18px;\n    border-bottom: ", ";\n    padding-bottom: 13px;\n"])), function (props) {
  return props.border ? '1px solid #ccc' : '';
});
var ExecutionPorcesses_styles_ActionItem = styled_components_browser_esm["b" /* default */].div(_templateObject28 || (_templateObject28 = taggedTemplateLiteral_default()(["\n    font-family: Roboto;\n    font-size: 16px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    letter-spacing: normal;\n    text-align: left;\n    color: #1483f3;\n    cursor: pointer;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/ExecutionPorcesses/index.tsx




function ExecutionPorcesses_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function ExecutionPorcesses_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ExecutionPorcesses_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : ExecutionPorcesses_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }















function ExecutionPorcesses(props) {
  var rows = props.rows,
    processType = props.processType,
    save = props.save,
    data = props.data;
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    options = _useState2[0],
    setOptions = _useState2[1];
  var _useState3 = Object(react["useState"])(false),
    _useState4 = slicedToArray_default()(_useState3, 2),
    open = _useState4[0],
    setOpen = _useState4[1];
  var ref = Object(react["useRef"])();
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    processesData = _useState6[0],
    setProcessesData = _useState6[1];
  var _useState7 = Object(react["useState"])([]),
    _useState8 = slicedToArray_default()(_useState7, 2),
    chosenProcesses = _useState8[0],
    setChosenProcesses = _useState8[1];
  var _useState9 = Object(react["useState"])([]),
    _useState10 = slicedToArray_default()(_useState9, 2),
    expandedRows = _useState10[0],
    setExpandedRows = _useState10[1];
  Object(react["useEffect"])(function () {
    function fetchExecutionProcessParam() {
      return _fetchExecutionProcessParam.apply(this, arguments);
    }
    function _fetchExecutionProcessParam() {
      _fetchExecutionProcessParam = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var params_data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getExecutionProcessParams(processType, rows.map(function (it) {
                return it.process_name;
              }));
            case 3:
              params_data = _context.sent;
              rows.map(function (it) {
                var found_params = params_data.find(function (it2) {
                  return it.process_name === it2.process_name;
                });
                it.editors = (found_params === null || found_params === void 0 ? void 0 : found_params.editors) || [];
                it.editors = it.editors.map(function (it) {
                  var _it$editor;
                  it.name = (_it$editor = it.editor) === null || _it$editor === void 0 ? void 0 : _it$editor.name;
                  if (it.default) {
                    it.value = it.default;
                    it.editor.value = it.value;
                  }
                  return it;
                });
              });
              (data || []).forEach(function (processData) {
                var foundRow = rows.find(function (it) {
                  return it.process_name === processData.process_name;
                });
                if (foundRow) {
                  var _processData$paramete;
                  processData.editors = foundRow.editors;
                  ((processData === null || processData === void 0 ? void 0 : (_processData$paramete = processData.parameters) === null || _processData$paramete === void 0 ? void 0 : _processData$paramete.inputs) || []).forEach(function (param) {
                    var foundEditor = processData.editors.find(function (it) {
                      return it.name === param.name;
                    });
                    if (foundEditor) {
                      foundEditor.value = param.value || null;
                      foundEditor.editor.value = param.value || null;
                      foundEditor.editor.schema2 = param.schema || null;
                    }
                  });
                }
              });
              setProcessesData(toConsumableArray_default()(data));
              setOptions(rows);
              _context.next = 12;
              break;
            case 10:
              _context.prev = 10;
              _context.t0 = _context["catch"](0);
            case 12:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 10]]);
      }));
      return _fetchExecutionProcessParam.apply(this, arguments);
    }
    fetchExecutionProcessParam();
  }, [rows]);
  var expandRow = Object(react["useCallback"])(function (process_id) {
    setExpandedRows(function (oldArray) {
      if (oldArray.indexOf(process_id) >= 0) {
        return oldArray.filter(function (it) {
          return it !== process_id;
        });
      } else {
        return [].concat(toConsumableArray_default()(oldArray), [process_id]);
      }
    });
  }, [setExpandedRows]);
  var updateFabricEditorValues = function updateFabricEditorValues(processName, values) {
    values.forEach(function (data) {
      updateParamsValue(processName, data.name, data.value, data.schema);
    });
  };
  var updateParamsValue = Object(react["useCallback"])(function (processName, name, value, schema) {
    var processData = processesData.find(function (it) {
      return it.process_name === processName;
    });
    if (!processData) {
      return;
    }
    var newParams = processData.editors;
    var index = newParams.findIndex(function (param) {
      return param.name === name;
    });
    if (index >= 0) {
      newParams[index].value = value;
      newParams[index].schema = schema;
      newParams[index].editor.value = value;
      var parameters = {
        inputs: (newParams || []).map(function (it) {
          return {
            name: it.name,
            type: it.type,
            value: it.value,
            schema: it.schema
          };
        })
      };
      processData.parameters = parameters;
      processData.edited = true;
      setProcessesData(toConsumableArray_default()(processesData));
    }
  }, [processesData, save, processType]);
  var getProcessEditors = Object(react["useCallback"])(function (processName) {
    var processData = processesData.find(function (it) {
      return it.process_name === processName;
    });
    if (!processData) {
      return [];
    }
    return processData.editors.map(function (it) {
      if (it.editor && it.editor.value === undefined) {
        it.editor.value = null;
      }
      return it.editor;
    });
  }, [processesData]);
  var closeModal = Object(react["useCallback"])(function () {
    setOpen(false);
    setChosenProcesses([]);
  }, [setOpen]);
  var addProcess = Object(react["useCallback"])(function () {
    if (chosenProcesses.length > 0) {
      setProcessesData(function (oldArray) {
        return [].concat(toConsumableArray_default()(oldArray), toConsumableArray_default()(chosenProcesses));
      });
    }
    setOpen(false);
    setChosenProcesses([]);
  }, [setOpen, setProcessesData, setChosenProcesses, chosenProcesses]);
  var deleteProcess = Object(react["useCallback"])(function (processName) {
    setProcessesData(function (oldArray) {
      return oldArray.filter(function (it) {
        return it.process_name !== processName;
      });
    });
  }, [setProcessesData]);
  var onProcessChange = Object(react["useCallback"])(function (value) {
    setChosenProcesses(value);
  }, [setOpen, setProcessesData, setChosenProcesses, chosenProcesses]);
  Object(react["useEffect"])(function () {
    save(processType, processesData);
  }, [processesData]);
  var getAddProcessContainer = Object(react["useCallback"])(function () {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ModalAddContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Title, {
        children: ["".concat(processType === 'pre' ? 'Pre' : 'Post', " execution process"), /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_CloseIcon, {
          onClick: function onClick() {
            return closeModal();
          },
          src: xclose
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Body, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
          title: "",
          maxMenuHeight: 120,
          value: chosenProcesses,
          onChange: onProcessChange,
          options: (options || []).filter(function (it) {
            return processesData.findIndex(function (it2) {
              return it.process_name === it2.process_name;
            }) < 0;
          }).map(function (it) {
            return ExecutionPorcesses_objectSpread(ExecutionPorcesses_objectSpread({}, it), {}, {
              label: it.process_name,
              value: it.process_id
            });
          }),
          loading: false,
          isMulti: true,
          enableSelectAll: false
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Actions, {
          border: false,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_ActionItem, {
            onClick: function onClick() {
              return closeModal();
            },
            children: "Cancel"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_ActionItem, {
            onClick: function onClick() {
              return addProcess();
            },
            children: "Save"
          })]
        })]
      })]
    });
  }, [processType, options, setOpen, chosenProcesses, processesData]);
  var getMandatoryEditors = function getMandatoryEditors(processData) {
    return processData.editors.findIndex(function (it) {
      return it.mandatory && (it.value === null || it.value === undefined);
    }) >= 0;
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(AddButtonContainer, {
      ref: ref,
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
        containerStyle: {
          zIndex: '100'
        },
        reposition: false,
        padding: 10,
        align: "center",
        isOpen: open,
        positions: ['left'],
        content: getAddProcessContainer(),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          onClick: function onClick() {
            return setOpen(true);
          },
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
            title: 'Add Process',
            type: 'secondary',
            width: '150px',
            onClick: function onClick() {},
            backgroundColor: "trasnparent",
            icon: plus
          })
        })
      })
    }), (processesData === null || processesData === void 0 ? void 0 : processesData.length) > 0 ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_TableContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_Thead, {
        children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TheadRow, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TheadEmptyColumn, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(TheadNameColumn, {
            children: "Process name"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TheadOrderColumn, {
            children: "Execution order"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TheadDeleteColumn, {})]
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TBody, {
        children: processesData.sort(function (a, b) {
          return a.execution_order - b.execution_order;
        }).map(function (it) {
          return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
            children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TBodyRow, {
              expand: expandedRows.indexOf(it.process_id) >= 0,
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyEditColumn, {
                expand: expandedRows.indexOf(it.process_id) >= 0,
                children: getProcessEditors(it.process_name).length === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(EditIconContainer, {
                  expand: expandedRows.indexOf(it.process_id) >= 0,
                  onClick: function onClick() {
                    return expandRow(it.process_id);
                  },
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(EditIcon, {
                    mandatory: getMandatoryEditors(it),
                    edited: expandedRows.indexOf(it.process_id) >= 0 ? false : it.edited,
                    children: "\u270E"
                  })
                })
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyNameColumn, {
                expand: expandedRows.indexOf(it.process_id) >= 0,
                children: it.process_name
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyOrderColumn, {
                children: it.execution_order
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyDeleteColumn, {
                expand: expandedRows.indexOf(it.process_id) >= 0,
                children: /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_Icon, {
                  onClick: function onClick() {
                    deleteProcess(it.process_name);
                  },
                  src: delete_icon_gray
                })
              })]
            }), expandedRows.indexOf(it.process_id) >= 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandRow, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandContainer, {
                children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandContent, {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(fabricWidget, {
                    updateValues: function updateValues(values) {
                      return updateFabricEditorValues(it.process_name, values);
                    },
                    editor: getProcessEditors(it.process_name),
                    saveRef: function saveRef(data) {}
                  }, "".concat(processType, "_execution_process"))
                })
              })
            }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
          });
        })
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var Advanced_ExecutionPorcesses = (ExecutionPorcesses);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/index.tsx


















function AdvancedForm(props) {
  var be_id = props.be_id;
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm;
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var postExecutionProcesses = taskData.postExecutionProcesses,
    preExecutionProcesses = taskData.preExecutionProcesses,
    scheduler = taskData.scheduler,
    globals = taskData.globals,
    sourceUserRole = taskData.sourceUserRole,
    userRole = taskData.userRole,
    enable_masking_only = taskData.enable_masking_only;
  console.log('taskData', taskData);
  var _useState = Object(react["useState"])(true),
    _useState2 = slicedToArray_default()(_useState, 2),
    preLoading = _useState2[0],
    setPreLoading = _useState2[1];
  var _useState3 = Object(react["useState"])(true),
    _useState4 = slicedToArray_default()(_useState3, 2),
    postLoading = _useState4[0],
    setPostLoading = _useState4[1];
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    preExecutionProcessOptions = _useState6[0],
    setPreExecutionProcessOptions = _useState6[1];
  var _useState7 = Object(react["useState"])([]),
    _useState8 = slicedToArray_default()(_useState7, 2),
    postExecutionProcessOptions = _useState8[0],
    setPostExecutionProcessOptions = _useState8[1];
  Object(react["useEffect"])(function () {
    function fetchPreExecutionProcess() {
      return _fetchPreExecutionProcess.apply(this, arguments);
    }
    function _fetchPreExecutionProcess() {
      _fetchPreExecutionProcess = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              if (be_id) {
                _context.next = 3;
                break;
              }
              return _context.abrupt("return");
            case 3:
              _context.next = 5;
              return apis_task.getPreExecutionProcess(be_id);
            case 5:
              data = _context.sent;
              setPreExecutionProcessOptions(data);
              setPreLoading(false);
              _context.next = 13;
              break;
            case 10:
              _context.prev = 10;
              _context.t0 = _context["catch"](0);
              // use hook toast
              setPreLoading(false);
            case 13:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 10]]);
      }));
      return _fetchPreExecutionProcess.apply(this, arguments);
    }
    function fetchPostExecutionProcess() {
      return _fetchPostExecutionProcess.apply(this, arguments);
    }
    function _fetchPostExecutionProcess() {
      _fetchPostExecutionProcess = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
        var data;
        return regenerator_default.a.wrap(function _callee2$(_context2) {
          while (1) switch (_context2.prev = _context2.next) {
            case 0:
              _context2.prev = 0;
              if (be_id) {
                _context2.next = 3;
                break;
              }
              return _context2.abrupt("return");
            case 3:
              _context2.next = 5;
              return apis_task.getPostExecutionProcess(be_id);
            case 5:
              data = _context2.sent;
              setPostExecutionProcessOptions(data);
              setPostLoading(false);
              _context2.next = 13;
              break;
            case 10:
              _context2.prev = 10;
              _context2.t0 = _context2["catch"](0);
              // use hook toast
              setPostLoading(false);
            case 13:
            case "end":
              return _context2.stop();
          }
        }, _callee2, null, [[0, 10]]);
      }));
      return _fetchPostExecutionProcess.apply(this, arguments);
    }
    fetchPreExecutionProcess();
    fetchPostExecutionProcess();
  }, []);
  var saveExecutionProcesses = Object(react["useCallback"])(function (processType, data) {
    saveForm(defineProperty_default()({}, "".concat(processType, "ExecutionProcesses"), data));
  }, [saveForm]);
  var getPreProcessBody = function getPreProcessBody() {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_ExecutionPorcesses, {
      rows: preExecutionProcessOptions,
      processType: 'pre',
      save: saveExecutionProcesses,
      data: preExecutionProcesses
    }, 'pre_execution_container');
  };
  var getPostProcessBody = Object(react["useCallback"])(function () {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_ExecutionPorcesses, {
      rows: postExecutionProcessOptions,
      processType: 'post',
      save: saveExecutionProcesses,
      data: postExecutionProcesses
    }, 'post_execution_container');
  }, [postExecutionProcesses, postExecutionProcessOptions, saveExecutionProcesses]);
  var tabs = Object(react["useMemo"])(function () {
    var result = [{
      name: 'Task variables'
    }];
    if (!enable_masking_only) {
      result.unshift({
        name: 'Post execution process'
      });
      result.unshift({
        name: 'Pre execution process'
      });
    }
    if ((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || userRole && userRole.userType === 'owner' || sourceUserRole && sourceUserRole.userType === 'owner' || (!userRole || userRole !== null && userRole !== void 0 && userRole.allowed_task_scheduling) && (!sourceUserRole || sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_task_scheduling) && (userRole || sourceUserRole)) {
      result.push({
        name: 'Scheduler',
        icon: clock_icon
      });
    }
    // if ((!userRole.allowed_task_scheduling))
    // if ((!userRole || userRole.allowed_task_scheduling) &&
    //     (!userRole && sourceUserRole && sourceUserRole.allowed_task_scheduling)) {
    //     result.push({
    //         name: 'Scheduler',
    //         icon: clockIcon,
    //     });
    // }
    return result;
  }, [sourceUserRole, userRole, enable_masking_only]);
  var _useState9 = Object(react["useState"])('Pre execution process'),
    _useState10 = slicedToArray_default()(_useState9, 2),
    selectedTab = _useState10[0],
    setSelectedTab = _useState10[1];
  var changedTabs = Object(react["useMemo"])(function () {
    var result = [];
    if (scheduler !== 'immediate') {
      result.push('Scheduler');
    }
    if (globals && globals.length > 0) {
      result.push('Task variables');
    }
    if (postExecutionProcesses && postExecutionProcesses.length > 0) {
      result.push('Post execution process');
    }
    if (preExecutionProcesses && preExecutionProcesses.length > 0) {
      result.push('Pre execution process');
    }
    return result;
  }, [scheduler, globals, postExecutionProcesses, preExecutionProcesses]);
  var getSelectedTab = Object(react["useCallback"])(function () {
    if (selectedTab === 'Pre execution process') {
      return getPreProcessBody();
    } else if (selectedTab === 'Post execution process') {
      return getPostProcessBody();
    } else if (selectedTab === 'Task variables') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_TaskVariables, {});
    } else if (selectedTab === 'Scheduler') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Scheduler, {});
    }
  }, [selectedTab, getPreProcessBody, getPostProcessBody, preExecutionProcessOptions, postExecutionProcessOptions]);
  var onReset = Object(react["useCallback"])(function () {
    if (selectedTab === 'Pre execution process') {
      saveForm({
        preExecutionProcesses: []
      });
      setPreExecutionProcessOptions(toConsumableArray_default()(preExecutionProcessOptions));
    } else if (selectedTab === 'Post execution process') {
      saveForm({
        postExecutionProcesses: []
      });
      setPostExecutionProcessOptions(toConsumableArray_default()(postExecutionProcessOptions));
    } else if (selectedTab === 'Task variables') {
      saveForm({
        globals: []
      });
    } else if (selectedTab === 'Scheduler') {
      saveForm({
        scheduling_end_date: undefined,
        scheduler: 'immediate'
      });
    }
  }, [selectedTab, saveForm, postExecutionProcessOptions, preExecutionProcessOptions]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(Advanced_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_ResetButton, {
        onClick: onReset,
        children: ["Clear form", /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Icon, {
          src: revert_icon
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Tabs, {
        tabs: tabs,
        selected: selectedTab,
        changedTabs: changedTabs,
        setSelectedTab: setSelectedTab,
        children: getSelectedTab()
      })]
    })
  });
}
/* harmony default export */ var Advanced = (AdvancedForm);
// CONCATENATED MODULE: ./src/containers/Task/Froms/TaskTitle/styles.ts

var TaskTitle_styles_templateObject, TaskTitle_styles_templateObject2;

var TaskTitle_styles_Wrapper = styled_components_browser_esm["b" /* default */].div(TaskTitle_styles_templateObject || (TaskTitle_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 100%;\n    display: flex;\n    justify-content: center;\n\n"])));
var TaskTitle_styles_Container = styled_components_browser_esm["b" /* default */].div(TaskTitle_styles_templateObject2 || (TaskTitle_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    width: 635px;\n    display: flex;\n    flex-direction: column;\n    gap: 32px;\n"])));
// CONCATENATED MODULE: ./src/containers/Task/Froms/TaskTitle/index.tsx


function TaskTitle_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function TaskTitle_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? TaskTitle_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : TaskTitle_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }







function TaskTitleForm(props) {
  var _errors$task_title;
  var tasks_titles_active = props.tasks_titles_active;
  var _useContext = Object(react["useContext"])(TaskContext),
    taskData = _useContext.taskData,
    saveForm = _useContext.saveForm,
    register = _useContext.register,
    errors = _useContext.errors,
    copy = _useContext.copy;
  var task_title = taskData.task_title,
    task_description = taskData.task_description,
    task_id = taskData.task_id;
  var _useState = Object(react["useState"])(task_title || ''),
    _useState2 = slicedToArray_default()(_useState, 2),
    taskTitleLocal = _useState2[0],
    setTaskTitleLocal = _useState2[1];
  Object(react["useEffect"])(function () {
    setTaskTitleLocal(task_title || '');
  }, [task_title]);
  var taskTitleChange = Object(react["useCallback"])(function (taskTitle) {
    setTaskTitleLocal(taskTitle);
    saveForm({
      task_title: taskTitle,
      manual_title_change: true
    });
  }, [saveForm]);
  var validateTaskTitle = function validateTaskTitle(value) {
    if (task_id) {
      return true;
    }
    if (tasks_titles_active && tasks_titles_active.indexOf(value || '') >= 0) {
      return "Task # ".concat(value, " Already Exists");
    }
    return true;
  };
  var taskDescriptionChange = Object(react["useCallback"])(function (value) {
    saveForm({
      task_description: value
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTitle_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskTitle_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, TaskTitle_objectSpread(TaskTitle_objectSpread({}, register('task_title', {
        required: 'Please input a task title',
        pattern: {
          value: /^((?!_).)*$/,
          message: "Task title must not contain '_'"
        },
        validate: {
          taskTitleExist: validateTaskTitle
        }
      })), {}, {
        disabled: task_id && !copy,
        name: "task_title",
        placeholder: "Enter task name",
        mandatory: false,
        type: InputTypes.text,
        value: taskTitleLocal,
        onChange: taskTitleChange,
        title: "Task name",
        error: (_errors$task_title = errors.task_title) === null || _errors$task_title === void 0 ? void 0 : _errors$task_title.message
      })), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TextArea, {
        placeholder: "Type short description",
        name: "task_description",
        title: "Task description",
        value: task_description,
        onChange: taskDescriptionChange
      })]
    })
  });
}
/* harmony default export */ var TaskTitle = (TaskTitleForm);
// CONCATENATED MODULE: ./src/utils/utils.ts
var groupByField = function groupByField(data, field) {
  return data.reduce(function (acc, curr) {
    if (!acc[curr[field]]) acc[curr[field]] = []; //If this type wasn't previously stored
    acc[curr[field]].push(curr);
    return acc;
  }, {});
};
var uniqueByField = function uniqueByField(data, field) {
  return data.filter(function (item, index, array) {
    return array.findIndex(function (it) {
      return it[field] === item[field];
    }) === index;
  });
};
// CONCATENATED MODULE: ./src/containers/Task/Main/useLogicalUnits.ts







var useLogicalUnits_useLogicalUnits = function useLogicalUnits(initFinished, saveForm, initTask, dataSourceType, source_type, selected_logical_units_names, be_type, be_id, source_environment_id, environment_id) {
  var toast = hooks_useToast();
  var _useState = Object(react["useState"])([]),
    _useState2 = slicedToArray_default()(_useState, 2),
    sourceLogicalUnits = _useState2[0],
    setSourceLogicalUnits = _useState2[1];
  var _useState3 = Object(react["useState"])([]),
    _useState4 = slicedToArray_default()(_useState3, 2),
    targetLogicalUnits = _useState4[0],
    setTargetLogicalUnits = _useState4[1];
  var _useState5 = Object(react["useState"])([]),
    _useState6 = slicedToArray_default()(_useState5, 2),
    allLogicalUnits = _useState6[0],
    setAllLogicalUnits = _useState6[1];
  var getLogicalUnits = Object(react["useCallback"])( /*#__PURE__*/function () {
    var _ref = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee(be_id, setter, environment_id) {
      var data;
      return regenerator_default.a.wrap(function _callee$(_context) {
        while (1) switch (_context.prev = _context.next) {
          case 0:
            _context.prev = 0;
            _context.next = 3;
            return apis_task.getLogicalUnits(be_id, environment_id);
          case 3:
            data = _context.sent;
            data.forEach(function (item) {
              item.value = item.lu_id;
              item.label = item.lu_name;
            });
            setter(data);
            _context.next = 11;
            break;
          case 8:
            _context.prev = 8;
            _context.t0 = _context["catch"](0);
            // use hook toast
            console.error(_context.t0);
          case 11:
          case "end":
            return _context.stop();
        }
      }, _callee, null, [[0, 8]]);
    }));
    return function (_x, _x2, _x3) {
      return _ref.apply(this, arguments);
    };
  }(), []);
  Object(react["useEffect"])(function () {
    console.log('getLogicalUnits');
    if (!initFinished) {
      return;
    }
    if (be_id) {
      if (be_type === 'source' || source_environment_id) {
        getLogicalUnits(be_id, setSourceLogicalUnits, source_environment_id);
      }
    }
  }, [source_environment_id, initFinished, dataSourceType, be_type]);
  Object(react["useEffect"])(function () {
    if (!initFinished) {
      return;
    }
    if (be_id) {
      if (be_type === 'target' || environment_id) {
        getLogicalUnits(be_id, setTargetLogicalUnits, environment_id);
      }
    }
  }, [environment_id, initFinished, dataSourceType, be_type]);
  Object(react["useEffect"])(function () {
    if (!initFinished) {
      return;
    }
    if (be_id) {
      setTargetLogicalUnits([]);
      setSourceLogicalUnits([]);
      if (be_type === 'source') {
        getLogicalUnits(be_id, setSourceLogicalUnits, source_environment_id);
      } else if (be_type === 'target') {
        getLogicalUnits(be_id, setTargetLogicalUnits, environment_id);
      }
    }
  }, [be_id]);
  Object(react["useEffect"])(function () {
    var temp = (sourceLogicalUnits || []).concat(targetLogicalUnits || []);
    var allLus = uniqueByField(temp, 'lu_id').filter(function (it) {
      var sourceResult = sourceLogicalUnits.findIndex(function (sourceItem) {
        return sourceItem.lu_id === it.lu_id;
      }) >= 0;
      var targeResult = targetLogicalUnits.findIndex(function (targetItem) {
        return targetItem.lu_id === it.lu_id;
      }) >= 0;
      return (sourceResult || sourceLogicalUnits.length === 0) && (targeResult || targetLogicalUnits.length === 0);
    });
    setAllLogicalUnits(allLus);
    var removedLus = (selected_logical_units_names || []).filter(function (it) {
      return allLus.findIndex(function (it2) {
        return it2.lu_name === it;
      }) < 0;
    });
    if (removedLus.length > 0 && !initTask) {
      if (dataSourceType !== 'data_source' || source_type !== 'tables') {
        if (be_type === 'source' && source_environment_id || be_type === 'target' && environment_id) {
          toast.warning("The selected env does not contain the ".concat(removedLus, " ").concat(removedLus.length === 1 ? 'system' : 'systems', "."), true);
        }
      }
    }
    if (selected_logical_units_names && selected_logical_units_names.length > 0) {
      var tempLus = allLus.filter(function (it) {
        return selected_logical_units_names.indexOf(it.lu_name) >= 0;
      });
      if (tempLus.length > 0) {
        allLus = tempLus;
      }
    }
    saveForm({
      selected_logical_units: allLus.map(function (it) {
        return it.lu_id;
      }),
      selected_logical_units_names: allLus.map(function (it) {
        return it.lu_name;
      })
    });
  }, [sourceLogicalUnits, targetLogicalUnits]);
  return allLogicalUnits;
};
/* harmony default export */ var Main_useLogicalUnits = (useLogicalUnits_useLogicalUnits);
// CONCATENATED MODULE: ./src/containers/Task/Main/utils.ts



var defaultValues = ['task_title', 'be_id', 'task_id', 'be_name', 'environment_id', 'environment_name', 'replace_sequences', 'scheduler', 'scheduling_end_date', 'source_environment_id', 'source_environment_name', 'selection_method', 'version_ind', 'sync_mode', 'selection_param_value', 'num_of_entities', 'reserve_note', 'parameters', 'filterout_reserved', 'retention_period_type', 'retention_period_value', 'selected_version_task_name', 'selected_version_task_exe_id', 'delete_before_load', 'reserve_ind', 'clone_ind', 'load_entity', 'task_description', 'reserve_retention_period_type', 'reserve_retention_period_value', 'globals', 'tableList', 'custom_logic_lu_name', 'selected_subset_task_exe_id', 'task_status', 'task_created_by', 'owners', 'task_execution_status', 'execution_mode'];
var fieldsMapper = {
  'source_env_name': 'source_environment_name'
};

// 'load_entity'
// 'delete_before_load'
// 'reserve_ind'
// 'dataSourceType'
// 'source_type'
// 'mask_sensitive_data'
// 'dataSubsetType'
// 'tableList'
// 'target_env'
// 'selected_training_id'
// 'selected_training_name'
// 'synthetic_type'
// 'generation_type'
// 'retention_period_value'
// 'selectedVersionToLoad'
// 'entity_clone'
// 'selected_generation_id'
// 'customLogicParams'

var utils_convertTaskData = function convertTaskData(apiData, copy) {
  var taskData = {
    reserve_ind: false,
    delete_before_load: undefined,
    num_of_entities: 1,
    task_description: '',
    scheduler: 'immediate',
    filterout_reserved: 'OTHERS',
    execution_mode: 'INHERITED',
    selection_method: SelectionMethodEnum.L,
    parameters: '',
    refresh_reference_data: false,
    replace_sequences: false,
    load_entity: false,
    version_ind: false,
    retention_period_type: undefined,
    retention_period_value: undefined,
    reserve_retention_period_type: undefined,
    reserve_retention_period_value: undefined,
    selected_version_task_name: '',
    selected_version_datetime: '',
    selected_version_task_exe_id: undefined,
    selected_ref_version_task_name: '',
    selected_ref_version_datetime: '',
    selected_ref_version_task_exe_id: undefined,
    sync_mode: null,
    tableList: [],
    tables_selected: false,
    globals: [],
    reference: '',
    postExecutionProcesses: [],
    preExecutionProcesses: [],
    task_globals: false,
    dataGenerationParams: {},
    generateChosenParams: []
  };
  if (!apiData) {
    return taskData;
  }
  if (taskData.delete_before_load) {
    taskData.deleteWarning = false;
  } else {
    taskData.deleteWarning = true;
  }
  taskData.dataGenerationParams = apiData.generateParams;
  defaultValues.forEach(function (field) {
    taskData[field] = apiData[field];
  });
  if (taskData.clone_ind) {
    taskData.num_of_clones = apiData.num_of_entities;
  }
  if (taskData.selection_method === 'CLONE') {
    taskData.selection_method = SelectionMethodEnum.L;
  }
  Object.keys(fieldsMapper).forEach(function (field) {
    taskData[fieldsMapper[field]] = apiData[field];
  });
  taskData.be_type = 'source';
  if (apiData.task_type === 'TRAINING') {
    taskData.target_env = 'ai_training';
    taskData.dataSourceType = 'data_source';
    taskData.source_type = 'BE';
  } else if (apiData.task_type === 'AI_GENERATED') {
    taskData.dataSourceType = 'ai_generated';
    taskData.synthetic_type = 'new_data';
    taskData.environment_id = undefined;
    taskData.environment_name = undefined;
  } else if (apiData.task_type === 'EXTRACT') {
    taskData.environment_name = undefined;
    taskData.environment_id = undefined;
    taskData.dataSourceType = 'data_source';
    taskData.source_type = 'BE';
  } else if (apiData.task_type === 'RESERVE') {
    taskData.target_env = 'target_env';
    taskData.reserve_ind = true;
    taskData.be_type = 'target';
  } else if (apiData.task_type === 'DELETE') {
    taskData.target_env = 'target_env';
    taskData.delete_before_load = true;
    taskData.be_type = 'target';
  } else if (apiData.task_type === 'GENERATE') {
    taskData.dataSourceType = 'synthetic';
    taskData.synthetic_type = 'new_data';
    taskData.environment_id = undefined;
    taskData.environment_name = undefined;
  } else if (apiData.task_type === 'LOAD') {
    taskData.target_env = 'target_env';
    taskData.dataSourceType = 'data_source';
    taskData.source_type = 'BE';
    taskData.load_entity = true;
  }
  if (apiData.source_env_name === 'AI') {
    taskData.dataSourceType = 'ai_generated';
    taskData.synthetic_type = 'generated_data';
    if (apiData.selection_method === 'GENERATE_SUBSET') {
      taskData.generation_type = 'all';
    } else if (apiData.selection_method === 'AI_GENERATED') {
      taskData.synthetic_type = 'new_data';
    } else {
      taskData.generation_type = 'partial';
    }
  } else if (apiData.source_env_name === 'Synthetic') {
    taskData.dataSourceType = 'synthetic';
    taskData.synthetic_type = 'generated_data';
    if (apiData.selection_method === 'GENERATE_SUBSET') {
      taskData.generation_type = 'all';
    } else if (apiData.selection_method === 'GENERATE') {
      taskData.synthetic_type = 'new_data';
    } else {
      taskData.generation_type = 'partial';
    }
  }
  if (apiData.be_id === -1) {
    taskData.tables_selected = true;
    taskData.dataSourceType = 'data_source';
    taskData.source_type = 'tables';
  } else if (apiData.refcount > 0) {
    taskData.tables_selected = true;
  }
  if (copy && taskData.task_title) {
    taskData.task_title = taskData.task_title + ' Copy';
  }
  if (apiData.filterout_reserved === 'NA' || !apiData.filterout_reserved) {
    taskData.filterout_reserved = 'OTHERS';
  }
  return taskData;
};

/*
    refresh_reference_data*/

/*
    no in use
    generateParams  for generate
*/

/*
    field to be mapped
*/

/*
    Missing field
    task_globals
    selected_ref_version_task_name
    selected_ref_version_task_exe_id
    selected_ref_version_datetime
    globals
    refernce
    postExectionProcess
*/

var updateTaskType = function updateTaskType(taskData, data) {
  if (taskData.target_env === 'ai_training') {
    data.task_type = 'TRAINING';
  } else if (taskData.dataSourceType === 'data_source' && (!taskData.environment_id || !taskData.load_entity && !taskData.delete_before_load && !taskData.reserve_ind)) {
    data.task_type = 'EXTRACT';
  } else if (taskData.dataSourceType === 'ai_generated' && taskData.synthetic_type === 'new_data') {
    if (!taskData.environment_id || !taskData.load_entity && !taskData.delete_before_load && !taskData.reserve_ind) {
      data.task_type = 'AI_GENERATED';
      data.selection_method = 'AI_GENERATED';
      data.load_entity = false;
      data.environment_id = taskData.source_environment_id;
      data.environment_name = taskData.source_environment_name;
    } else {
      data.task_type = 'LOAD';
      data.selection_method = 'AI_GENERATED';
      data.load_entity = true;
    }
  } else if (taskData.dataSourceType === 'ai_generated' && taskData.synthetic_type === 'generated_data') {
    data.task_type = 'LOAD';
    data.load_entity = true;
    if (taskData.generation_type === 'all') {
      data.selection_method = 'GENERATE_SUBSET';
    }
  } else if (taskData.dataSourceType === 'synthetic' && taskData.synthetic_type === 'new_data') {
    if (!taskData.environment_id || !taskData.load_entity && !taskData.delete_before_load && !taskData.reserve_ind) {
      data.task_type = 'GENERATE';
      data.selection_method = 'GENERATE';
      data.load_entity = false;
      data.selected_subset_task_exe_id = 0;
      data.environment_id = taskData.source_environment_id;
      data.environment_name = taskData.source_environment_name;
    } else {
      data.task_type = 'LOAD';
      data.selection_method = 'GENERATE';
      data.load_entity = true;
      data.selected_subset_task_exe_id = 0;
    }
  } else if (taskData.dataSourceType === 'synthetic' && taskData.synthetic_type === 'generated_data') {
    data.task_type = 'LOAD';
    data.load_entity = true;
    if (taskData.generation_type === 'all') {
      data.selection_method = 'GENERATE_SUBSET';
    }
  } else if (taskData.reserve_ind && !taskData.load_entity) {
    data.task_type = 'RESERVE';
    data.source_environment_id = taskData.environment_id;
    data.source_env_name = taskData.environment_name;
    data.source_environment_name = taskData.environment_name;
  } else if (taskData.delete_before_load && !taskData.load_entity) {
    data.task_type = 'DELETE';
    data.source_environment_id = taskData.environment_id;
    data.source_env_name = taskData.environment_name;
    data.source_environment_name = taskData.environment_name;
  } else if (taskData.load_entity) {
    data.task_type = 'LOAD';
  }
};
var prepareDataForSave = function prepareDataForSave(taskData, logicalUnits, copy) {
  var data = {};
  var fieldsToCopy = ['task_id', 'be_id', 'postExecutionProcesses', 'preExecutionProcesses', 'environment_id', 'environment_name', 'source_environment_id', 'scheduler', 'num_of_entities', 'selection_method', 'selection_param_value', 'task_title', 'parameters', 'scheduling_end_date', 'version_ind', 'retention_period_value', 'retention_period_type', 'reserve_retention_period_type', 'reserve_retention_period_value', 'reserve_ind', 'load_entity', 'clone_ind', 'delete_before_load', 'reserve_note', 'selected_version_task_name', 'selected_version_datetime', 'selected_version_task_exe_id', 'filterout_reserved', 'mask_sensitive_data', 'replace_sequences', 'task_description', 'sync_mode', 'globals', 'reference', 'selected_ref_version_task_name', 'selected_ref_version_task_exe_id', 'selected_ref_version_datetime', 'task_globals', 'selected_subset_task_exe_id', 'custom_logic_lu_name', 'execution_mode'];
  fieldsToCopy.forEach(function (key) {
    data[key] = taskData[key];
  });
  if (taskData.tables_selected) {
    data.tableList = taskData.tableList;
  }
  if (taskData.clone_ind) {
    data.num_of_entities = taskData.num_of_clones;
  }
  data.generateParams = taskData.dataGenerationParams;
  updateTaskType(taskData, data);
  if (data.globals && data.globals.length > 0) {
    data.task_globals = true;
  } else {
    data.task_globals = false;
  }
  data.copy = copy;
  if (data.task_type !== 'RESERVE' && data.task_type !== 'DELETE') {
    data.source_env_name = taskData.source_environment_name;
  }
  var selectedLogicalUnits = logicalUnits.filter(function (it) {
    return ((taskData === null || taskData === void 0 ? void 0 : taskData.selected_logical_units) || []).indexOf(it.lu_id) >= 0;
  });
  data.logicalUnits = selectedLogicalUnits === null || selectedLogicalUnits === void 0 ? void 0 : selectedLogicalUnits.map(function (it) {
    return {
      lu_name: it.lu_name,
      lu_id: it.lu_id
    };
  });
  if ((taskData.clone_ind || taskData.replace_sequences) && !taskData.load_entity || taskData.target_env === 'ai_training' || !taskData.environment_id || !(taskData.sync_mode === 'OFF' && taskData.version_ind) && taskData.selection_method === 'ALL') {
    data.filterout_reserved = 'NA';
  }
  return data;
};
var getIfTables = function getIfTables(taskData) {
  if (taskData.dataSourceType === 'data_source') {
    if (taskData.source_type === 'tables') {
      return 'tables';
    } else if (taskData.tables_selected) {
      return 'be_tables';
    }
  }
  return '';
};
var getSourceInfo = function getSourceInfo(taskData) {
  var result = [];
  var isTables = getIfTables(taskData);
  if (taskData.dataSourceType === 'data_source') {
    if (taskData.source_environment_id) {
      if (taskData.version_ind) {
        if (isTables) {
          result.push('Get pre-created data snapshot for the entities and the tables.');
        } else {
          result.push('Get a pre-created data snapshot for the entities.');
        }
      } else if (isTables === 'be_tables') {
        result.push('Get entities\' and tables\' data.');
      } else if (isTables === 'tables') {
        result.push('Get tables\' data.');
      } else {
        result.push('Get entities\' data.');
      }
    }
  } else if (taskData.dataSourceType === 'synthetic') {
    if (taskData.synthetic_type === 'new_data') {
      result.push("Generate ".concat(taskData.num_of_entities || 0, " entities."));
    } else {
      result.push('Get generated entities from the TDM Data store');
    }
  } else if (taskData.dataSourceType === 'ai_generated') {
    if (taskData.synthetic_type === 'new_data') {
      result.push("Generate ".concat(taskData.num_of_entities || 0, " entities."));
    } else {
      result.push('Get generated entities from the TDM Data store');
    }
  }
  return result;
};
var getTargetInfo = function getTargetInfo(taskData) {
  var result = [];
  var isTables = getIfTables(taskData);
  if (taskData.load_entity && !taskData.reserve_ind && !taskData.delete_before_load) {
    if (!taskData.clone_ind) {
      if (taskData.replace_sequences) {
        if (isTables) {
          result.push('Load entities and replace their IDs. Load related tables.');
        } else {
          result.push('Load entities and replace their IDs');
        }
      } else if (!taskData.replace_sequences) {
        if (isTables === 'tables') {
          result.push('Load tables.');
        } else if (isTables === 'be_tables') {
          result.push('Load entities and tables.');
        } else {
          result.push('Load entities.');
        }
      }
    } else if (taskData.clone_ind) {
      if (isTables) {
        result.push("Create ".concat(taskData.num_of_entities || 0, " entity clones and load tables. "));
      } else {
        result.push("Create ".concat(taskData.num_of_entities || 0, " entity clones"));
      }
    } else if (taskData.version_ind && taskData.sync_mode === 'OFF') {
      result.push('Delete and reload the selected entity data snapshot');
    }
  } else if (taskData.load_entity && taskData.reserve_ind && !taskData.delete_before_load) {
    if (!taskData.clone_ind) {
      if (taskData.replace_sequences) {
        if (isTables) {
          result.push('Load entities and replace their IDs. Load the related tables. Reserve the newly created entities.');
        } else {
          result.push('Load entities and replace their IDs. Reserve the newly created entities');
        }
      } else if (!taskData.replace_sequences) {
        if (isTables) {
          result.push('Load entities and the related tables. Reserve the loaded entities. ');
        } else {
          result.push('Load and reserve entities');
        }
      }
    } else if (taskData.clone_ind) {
      if (isTables) {
        result.push("Create ".concat(taskData.num_of_entities, " entity clones and load the related tables. Reserve the newly created entity clones."));
      } else {
        result.push("Create ".concat(taskData.num_of_entities, " entity clones. Reserve the newly created entity clones"));
      }
    } else if (taskData.version_ind && taskData.sync_mode === 'OFF') {
      if (isTables) {
        result.push('Delete and reload the selected data snapshot for the entities and the related tables. Reserve the reloaded entities.');
      } else {
        result.push('Delete and reload the selected data snapshot. Reserve the reloaded entities');
      }
    }
  } else if (taskData.load_entity && !taskData.reserve_ind && taskData.delete_before_load) {
    if (!taskData.clone_ind) {
      if (taskData.replace_sequences) {
        result.push('Delete and Load entities. Replacing the IDs of the loaded entities on target');
      } else if (!taskData.replace_sequences) {
        if (isTables === 'tables') {
          result.push('Delete and reload tables.');
        } else if (isTables === 'be_tables') {
          result.push('Delete and reload entities and the related tables.');
        } else {
          result.push('Delete and reload entities.');
        }
      }
    } else if (taskData.clone_ind) {
      result.push("Delete the entity from the target and create in the target ".concat(taskData.num_of_entities, " clones for the entity"));
    }
  } else if (taskData.load_entity && taskData.reserve_ind && taskData.delete_before_load) {
    if (!taskData.clone_ind) {
      if (taskData.replace_sequences) {
        result.push('Delete and Load entities. Replacing the IDs of the loaded entities on target. Reserve the new entity IDs');
      } else if (!taskData.replace_sequences) {
        if (isTables) {
          result.push('Delete and reload entities and related tables. Reserve the reloaded entities.');
        } else {
          result.push('Delete and reload entities. Reserve the reloaded entities.');
        }
      }
    } else if (taskData.clone_ind) {
      result.push("Delete the entity from the target and create in the target ".concat(taskData.num_of_entities, " clones for the entity. Reserve the newly created entity clones"));
    }
  } else if (!taskData.load_entity && !taskData.reserve_ind && taskData.delete_before_load) {
    result.push('Delete entities');
  } else if (!taskData.load_entity && taskData.reserve_ind && !taskData.delete_before_load) {
    result.push('Reserve entities');
  }
  return result;
};
var utils_getSubsetInfo = function getSubsetInfo(taskData) {
  var result = [];
  var isTables = getIfTables(taskData);
  if (isTables === 'tables') {
    var _ref;
    result.push("Get ".concat((taskData.tableList || []).length, " tables. "));
    var filteredTables = (_ref = taskData.tableList || []) === null || _ref === void 0 ? void 0 : _ref.filter(function (it) {
      return it.table_filter && it.table_filter !== '()';
    });
    if (filteredTables.length > 0) {
      result.push("Number of tables with data filtering: ".concat(filteredTables.length, ". "));
    }
  } else if (taskData.selection_method === 'ALL') {
    result.push('Get a predefined entity list');
  } else if (taskData.version_ind && taskData.sync_mode === 'OFF') {
    result.push('Select a data snapshot.');
  } else if (taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'generated_data') {
    if (taskData.dataSourceType === 'synthetic') {
      result.push('Select a data generation execution');
    } else if (taskData.dataSourceType === 'ai_generated') {
      result.push('Select a data generation execution.');
    }
  } else {
    var selection_method = taskData.selection_method !== SelectionMethodEnum.PR ? taskData.selection_method : SelectionMethodEnum.P;
    var foundSelectionMethod = entitySeletionMethods.find(function (it) {
      return it.value === selection_method;
    });
    if (foundSelectionMethod && taskData.num_of_entities && foundSelectionMethod.label) {
      result.push("Get ".concat(taskData.num_of_entities || '(empty)', " entities based on a/an ").concat(foundSelectionMethod.label, " selection method."));
    }
  }
  return result;
};
var utils_getTestDataStoreInfo = function getTestDataStoreInfo(taskData, testDataStoreStatus, subsetStatus, subsetPosition) {
  if (subsetPosition === SubsetPossition.target && subsetStatus !== StatusEnum.disabled) {
    return ['Get data from the TDM Data store.'];
  }
  if (testDataStoreStatus !== StatusEnum.completed) {
    return [];
  }
  var result = [];
  if (taskData.version_ind) {
    if (taskData.retention_period_type === 'Do Not Delete') {
      result.push('Create a data snapshot (version). Save the data in the TDM Data store for an unlimited period.');
    } else {
      result.push('Create a data snapshot (version). Save the data in the TDM Data store for a limited period.');
    }
  } else {
    if (taskData.retention_period_type === 'Do Not Delete') {
      result.push('Save the data in the TDM Data store for an unlimited period.');
    } else if (taskData.retention_period_type === 'Do Not Retain') {
      result.push('Do not save the data in the TDM Data store.');
    } else {
      result.push('Save the data in the TDM Data store for a limited period.');
    }
  }
  return result;
};
var utils_getTaskTitle = function getTaskTitle(taskData) {
  var be_name = taskData.be_name,
    be_id = taskData.be_id,
    tables_selected = taskData.tables_selected,
    manual_title_change = taskData.manual_title_change,
    task_title = taskData.task_title;
  if (manual_title_change) {
    return task_title || '';
  }
  var new_task_title = "";
  console.log(be_name);
  if (be_id === -1) {
    new_task_title = new_task_title + "tables ";
  } else if (be_id) {
    if (tables_selected) {
      new_task_title = new_task_title + "".concat(be_name, " and tables ");
    } else {
      new_task_title = new_task_title + "".concat(be_name, " ");
    }
  } else {
    return '';
  }
  new_task_title = new_task_title + moment_default()(new Date()).format('DD-MM-YYYY HH:mm:ss');
  return new_task_title;
};
// CONCATENATED MODULE: ./src/components/task/TaskActions/styles.ts

var TaskActions_styles_templateObject, TaskActions_styles_templateObject2, TaskActions_styles_templateObject3, TaskActions_styles_templateObject4, TaskActions_styles_templateObject5, TaskActions_styles_templateObject6, TaskActions_styles_templateObject7, TaskActions_styles_templateObject8;

var TaskActions_styles_Container = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject || (TaskActions_styles_templateObject = taggedTemplateLiteral_default()(["\n    width: 300px;\n    position: absolute;\n    top: 29px;\n    left: 50px;\n"])));
var ButtonsContainer = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject2 || (TaskActions_styles_templateObject2 = taggedTemplateLiteral_default()(["\n    height: 60px;\n    display: flex;\n    margin-top: 16px;\n    border-left: 2px solid #e5e5e5;\n"])));
var Action = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject3 || (TaskActions_styles_templateObject3 = taggedTemplateLiteral_default()(["\n    width: 99px;\n    display: ", ";\n    flex-direction: column;\n    gap: 7px;\n    align-items: center;\n    padding: 0px 15px;\n    border-right: 2px solid #e5e5e5;\n    cursor: pointer;\n"])), function (props) {
  return props.hide ? 'none' : 'flex';
});
var ActionText = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject4 || (TaskActions_styles_templateObject4 = taggedTemplateLiteral_default()(["\n  font-family: Roboto;\n  font-size: 12px;\n  font-weight: 500;\n  font-stretch: normal;\n  font-style: normal;\n  line-height: 1;\n  letter-spacing: normal;\n  text-align: center;\n  color: #2e2e2e;\n\n"])));
var TaskTitleContainer = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject5 || (TaskActions_styles_templateObject5 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    font-family: Roboto;\n    font-size: 15px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.33;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    padding-bottom: 17px;\n    white-space: nowrap;\n    overflow: hidden;\n    text-overflow: ellipsis;\n    width: 100%;\n"])));
var EntitiesAndTablesContainer = styled_components_browser_esm["b" /* default */].div(TaskActions_styles_templateObject6 || (TaskActions_styles_templateObject6 = taggedTemplateLiteral_default()(["\n    cursor: pointer;\n    min-height: 39px;\n    display: flex;\n    gap: 15px;\n    align-items: center;\n    font-family: Roboto;\n    font-size: 15px;\n    font-weight: normal;\n    font-stretch: normal;\n    font-style: normal;\n    line-height: 1.33;\n    letter-spacing: normal;\n    text-align: left;\n    color: #2e2e2e;\n    border-bottom: 2px solid #e5e5e5;\n    padding-bottom: 17px;\n    white-space: nowrap;\n    overflow: hidden;\n    text-overflow: ellipsis;\n    width: 100%;\n"])));
var TaskTitleButton = styled_components_browser_esm["b" /* default */].span(TaskActions_styles_templateObject7 || (TaskActions_styles_templateObject7 = taggedTemplateLiteral_default()(["\n    color: #1683f2;\n"])));
var TaskActions_styles_Icon = styled_components_browser_esm["b" /* default */].img(TaskActions_styles_templateObject8 || (TaskActions_styles_templateObject8 = taggedTemplateLiteral_default()(["\n    width: 25px;\n"])));
// CONCATENATED MODULE: ./src/images/settings-icon.svg
/* harmony default export */ var settings_icon = ("js/dist/c91068a2c13098ab1c04ec54f77536a9.svg");
// CONCATENATED MODULE: ./src/images/save-icon.svg
/* harmony default export */ var save_icon = ("js/dist/a5e1a483f3b25d0de3786b1759104171.svg");
// CONCATENATED MODULE: ./src/images/save-exe-icon.svg
/* harmony default export */ var save_exe_icon = ("js/dist/a6172cf3de51586156f0bf4ab2d4d11d.svg");
// CONCATENATED MODULE: ./src/components/task/TaskActions/index.tsx









function TaskActions(props) {
  var setCurrentStep = props.setCurrentStep,
    task_title = props.task_title,
    saveLocalData = props.saveLocalData,
    closeTask = props.closeTask,
    saveTask = props.saveTask,
    saveAndExecute = props.saveAndExecute,
    deleteTask = props.deleteTask,
    tables_selected = props.tables_selected,
    be_name = props.be_name,
    disableChange = props.disableChange,
    task_execution_status = props.task_execution_status;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskActions_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskTitleContainer, {
      onClick: function onClick() {
        return setCurrentStep('task_title');
      },
      children: ["Task name: ", /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTitleButton, {
        children: task_title
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(EntitiesAndTablesContainer, {
      children: [be_name || tables_selected && 'Tables' || '', be_name ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
        title: "Entity",
        style: {
          width: '10px'
        },
        src: entity_icon
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), tables_selected ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
        title: "Tables",
        style: {
          width: '14px'
        },
        src: table_icon
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ButtonsContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Action, {
        onClick: function onClick() {
          return saveTask();
        },
        hide: disableChange,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
          src: save_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionText, {
          children: "Save"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Action, {
        onClick: function onClick() {
          return saveAndExecute();
        },
        hide: disableChange || task_execution_status == 'onHold',
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
          src: save_exe_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionText, {
          children: "Save & execute"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Action, {
        onClick: function onClick() {
          return setCurrentStep('be_advanced');
        },
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
          src: settings_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionText, {
          children: "Advanced settings"
        })]
      })]
    })]
  });
}
/* harmony default export */ var task_TaskActions = (TaskActions);
// CONCATENATED MODULE: ./src/containers/Task/Main/useRoles.ts








var useRoles_useRoles = function useRoles(saveForm, taskData) {
  var authService = getService('AuthService');
  var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  var userId = authService === null || authService === void 0 ? void 0 : authService.getUserId();
  var _useState = Object(react["useState"])(null),
    _useState2 = slicedToArray_default()(_useState, 2),
    userFabricRoles = _useState2[0],
    setUserFabricRoles = _useState2[1];
  Object(react["useEffect"])(function () {
    function fetchFabricRoles() {
      return _fetchFabricRoles.apply(this, arguments);
    }
    function _fetchFabricRoles() {
      _fetchFabricRoles = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.next = 2;
              return apis_task.getFabricRolesByUser(userId);
            case 2:
              data = _context.sent;
              setUserFabricRoles(data || []);
            case 4:
            case "end":
              return _context.stop();
          }
        }, _callee);
      }));
      return _fetchFabricRoles.apply(this, arguments);
    }
    fetchFabricRoles();
  }, []);
  var source_environment_id = taskData.source_environment_id,
    environment_id = taskData.environment_id,
    reserve_ind = taskData.reserve_ind,
    delete_before_load = taskData.delete_before_load,
    load_entity = taskData.load_entity,
    targetEnvOwner = taskData.targetEnvOwner,
    sourceEnvOwner = taskData.sourceEnvOwner,
    maxToCopy = taskData.maxToCopy,
    maxToCopyType = taskData.maxToCopyType,
    userRole = taskData.userRole,
    sync_mode = taskData.sync_mode;
  var getRoleForUserInEnv = Object(react["useCallback"])( /*#__PURE__*/function () {
    var _ref = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2(env_id, isSource) {
      var data, updateData, temp_data, task_type, minRead, minWrite;
      return regenerator_default.a.wrap(function _callee2$(_context2) {
        while (1) switch (_context2.prev = _context2.next) {
          case 0:
            if (!(isSource && sourceEnvOwner || !isSource && targetEnvOwner)) {
              _context2.next = 2;
              break;
            }
            return _context2.abrupt("return");
          case 2:
            _context2.next = 4;
            return apis_task.getEnvironmentUserRole(env_id);
          case 4:
            data = _context2.sent;
            updateData = defineProperty_default()({}, isSource ? 'sourceUserRole' : 'userRole', data.userRole);
            updateData[isSource ? 'sourceUserRole' : 'userRole'].userType = 'tester';
            temp_data = {};
            updateTaskType(taskData, temp_data);
            task_type = temp_data.task_type;
            minRead = parseInt(data.minRead || '0');
            minWrite = parseInt(data.minWrite || '0');
            if (minRead > -1 || minWrite > -1) {
              if (isSource) {
                if (sync_mode !== 'OFF') {
                  if (maxToCopy && maxToCopy > minRead || !maxToCopy && minRead > -1) {
                    updateData.maxToCopy = minRead;
                    updateData.maxToCopyType = 'source';
                  }
                } else if (maxToCopyType === 'source') {
                  updateData.maxToCopy = undefined;
                }
              } else {
                if (maxToCopy && maxToCopy > minWrite || !maxToCopy && minWrite > -1) {
                  updateData.maxToCopy = minWrite;
                  updateData.maxToCopyType = 'target';
                }
              }
            }
            if (reserve_ind && !load_entity && !isSource && data.userRole) {
              updateData.maxToCopy = data.userRole.allowed_number_of_reserved_entities;
            }
            if (parseInt(data.minWrite || '0') === 0 && data.userRole.allowed_number_of_reserved_entities > 0) {
              updateData.reserve_only_task = true;
            }
            saveForm(updateData);
          case 16:
          case "end":
            return _context2.stop();
        }
      }, _callee2);
    }));
    return function (_x, _x2) {
      return _ref.apply(this, arguments);
    };
  }(), [sourceEnvOwner, targetEnvOwner, maxToCopy, maxToCopyType, userRole, saveForm, reserve_ind, load_entity, sync_mode]);
  var getEnvironmentOwners = Object(react["useCallback"])( /*#__PURE__*/function () {
    var _ref2 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee3(env_id, isSource) {
      var data, ownerFound, _loop, i, _saveForm;
      return regenerator_default.a.wrap(function _callee3$(_context4) {
        while (1) switch (_context4.prev = _context4.next) {
          case 0:
            _context4.next = 2;
            return apis_task.getEnvironmentOwners(env_id);
          case 2:
            data = _context4.sent;
            ownerFound = data.find(function (it) {
              return it.user_id === userId;
            });
            if (ownerFound) {
              _context4.next = 14;
              break;
            }
            _loop = /*#__PURE__*/regenerator_default.a.mark(function _loop(i) {
              var result;
              return regenerator_default.a.wrap(function _loop$(_context3) {
                while (1) switch (_context3.prev = _context3.next) {
                  case 0:
                    result = data.find(function (it) {
                      return it.user_type === 'GROUP' && it.user_id === userFabricRoles[i];
                    });
                    if (!result) {
                      _context3.next = 4;
                      break;
                    }
                    ownerFound = result;
                    return _context3.abrupt("return", 1);
                  case 4:
                  case "end":
                    return _context3.stop();
                }
              }, _loop);
            });
            i = 0;
          case 7:
            if (!(i < userFabricRoles.length)) {
              _context4.next = 14;
              break;
            }
            return _context4.delegateYield(_loop(i), "t0", 9);
          case 9:
            if (!_context4.t0) {
              _context4.next = 11;
              break;
            }
            return _context4.abrupt("break", 14);
          case 11:
            i++;
            _context4.next = 7;
            break;
          case 14:
            if (!ownerFound) {
              _context4.next = 19;
              break;
            }
            saveForm((_saveForm = {}, defineProperty_default()(_saveForm, isSource ? 'sourceUserRole' : 'userRole', {
              allowed_random_entity_selection: true,
              allowed_creation_of_synthetic_data: true,
              allowed_refresh_reference_data: true,
              allowed_request_of_fresh_data: true,
              allowed_delete_before_load: true,
              allowed_task_scheduling: true,
              allowed_replace_sequences: true,
              allow_read: true,
              allow_write: true,
              userType: 'owner'
            }), defineProperty_default()(_saveForm, "maxToCopy", 9007199254740992), defineProperty_default()(_saveForm, isSource ? 'sourceEnvOwner' : 'targetEnvOwner', true), _saveForm));
            return _context4.abrupt("return", isSource ? 'source_owner' : 'target_owner');
          case 19:
            saveForm(defineProperty_default()({}, isSource ? 'sourceEnvOwner' : 'targetEnvOwner', false));
            return _context4.abrupt("return", isSource ? 'not_source_owner' : 'not_target_owner');
          case 22:
          case "end":
            return _context4.stop();
        }
      }, _callee3);
    }));
    return function (_x3, _x4) {
      return _ref2.apply(this, arguments);
    };
  }(), [userFabricRoles, saveForm, userId]);
  Object(react["useEffect"])(function () {
    if (reserve_ind && !load_entity && userRole && userRole.userType === 'tester') {
      saveForm({
        maxToCopy: userRole.allowed_number_of_reserved_entities
      });
    }
  }, [reserve_ind]);
  Object(react["useEffect"])(function () {
    if (!userFabricRoles) {
      return;
    }
    if ((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || "production" === 'development') {
      saveForm({
        sourceUserRole: {
          allowed_random_entity_selection: true,
          allowed_creation_of_synthetic_data: true,
          allowed_refresh_reference_data: true,
          allowed_request_of_fresh_data: true,
          allowed_delete_before_load: true,
          allowed_task_scheduling: true,
          allowed_replace_sequences: true,
          allow_read: true,
          allow_write: true,
          userType: 'admin'
        },
        userRole: {
          allowed_random_entity_selection: true,
          allowed_creation_of_synthetic_data: true,
          allowed_refresh_reference_data: true,
          allowed_request_of_fresh_data: true,
          allowed_delete_before_load: true,
          allowed_task_scheduling: true,
          allowed_replace_sequences: true,
          allow_read: true,
          allow_write: true,
          userType: 'admin'
        },
        maxToCopy: 9007199254740992
      });
    } else {
      var promises = [];
      if (!source_environment_id) {
        saveForm({
          sourceUserRole: undefined
        });
      } else if (source_environment_id && !(reserve_ind && !load_entity || delete_before_load && !load_entity)) {
        promises.push(getEnvironmentOwners(source_environment_id, true));
      }
      if (environment_id) {
        promises.push(getEnvironmentOwners(environment_id, false));
      } else {
        saveForm({
          userRole: undefined
        });
      }
      Promise.all(promises).then( /*#__PURE__*/function () {
        var _ref3 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee4(result) {
          return regenerator_default.a.wrap(function _callee4$(_context5) {
            while (1) switch (_context5.prev = _context5.next) {
              case 0:
                if (!(result.indexOf('not_source_owner') >= 0 && source_environment_id && !(reserve_ind && !load_entity || delete_before_load && !load_entity))) {
                  _context5.next = 3;
                  break;
                }
                _context5.next = 3;
                return getRoleForUserInEnv(source_environment_id, true);
              case 3:
                if (!(result.indexOf('not_target_owner') >= 0 && environment_id)) {
                  _context5.next = 6;
                  break;
                }
                _context5.next = 6;
                return getRoleForUserInEnv(environment_id, false);
              case 6:
              case "end":
                return _context5.stop();
            }
          }, _callee4);
        }));
        return function (_x5) {
          return _ref3.apply(this, arguments);
        };
      }());
    }
  }, [userFabricRoles, source_environment_id, environment_id, load_entity, reserve_ind, delete_before_load, sync_mode]);

  // useEffect(() => {
  //     saveForm({
  //         userRole: {
  //             allowed_random_entity_selection: systemUserRole?.type !== 'tester',
  //             allowed_creation_of_synthetic_data: systemUserRole?.type !== 'tester',
  //             allowed_refresh_reference_data: systemUserRole?.type !== 'tester',
  //             allowed_request_of_fresh_data: systemUserRole?.type !== 'tester',
  //             allowed_delete_before_load: systemUserRole?.type !== 'tester',
  //             allowed_task_scheduling: systemUserRole?.type !== 'tester',
  //             allowed_replace_sequences: systemUserRole?.type !== 'tester',
  //             default: true,
  //         },
  //         sourceUserRole: {
  //             allowed_random_entity_selection: systemUserRole?.type !== 'tester',
  //             allowed_creation_of_synthetic_data: systemUserRole?.type !== 'tester',
  //             allowed_refresh_reference_data: systemUserRole?.type !== 'tester',
  //             allowed_request_of_fresh_data: systemUserRole?.type !== 'tester',
  //             allowed_delete_before_load: systemUserRole?.type !== 'tester',
  //             allowed_task_scheduling: systemUserRole?.type !== 'tester',
  //             allowed_replace_sequences: systemUserRole?.type !== 'tester',
  //             default: true,
  //         },
  //         maxToCopy: 100000000,
  //     });
  // }, []);

  return {};
};
/* harmony default export */ var Main_useRoles = (useRoles_useRoles);
// CONCATENATED MODULE: ./src/containers/Task/Main/useInit.ts



function useInit_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function useInit_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? useInit_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : useInit_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }



var useInit_useInit = function useInit(saveForm, taskData) {
  Object(react["useEffect"])(function () {
    function fetchActiveBE() {
      return _fetchActiveBE.apply(this, arguments);
    }
    function _fetchActiveBE() {
      _fetchActiveBE = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
        var data, updateData;
        return regenerator_default.a.wrap(function _callee$(_context) {
          while (1) switch (_context.prev = _context.next) {
            case 0:
              _context.prev = 0;
              _context.next = 3;
              return apis_task.getActiveBEs();
            case 3:
              data = _context.sent;
              updateData = {
                enable_masking_only: !data || data.length === 0 ? true : false
              };
              if (updateData.enable_masking_only) {
                updateData.dataSourceType = 'data_source';
                updateData.source_type = 'tables';
                updateData.selection_method = 'TABLES';
                updateData.be_id = -1;
              }
              saveForm(updateData);
              _context.next = 11;
              break;
            case 9:
              _context.prev = 9;
              _context.t0 = _context["catch"](0);
            case 11:
            case "end":
              return _context.stop();
          }
        }, _callee, null, [[0, 9]]);
      }));
      return _fetchActiveBE.apply(this, arguments);
    }
    fetchActiveBE();
  }, []);
  Object(react["useEffect"])(function () {
    function fetchEnableParamsLUName() {
      return _fetchEnableParamsLUName.apply(this, arguments);
    }
    function _fetchEnableParamsLUName() {
      _fetchEnableParamsLUName = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2() {
        var result;
        return regenerator_default.a.wrap(function _callee2$(_context2) {
          while (1) switch (_context2.prev = _context2.next) {
            case 0:
              _context2.prev = 0;
              _context2.next = 3;
              return apis_task.getParamsLUName();
            case 3:
              result = _context2.sent;
              saveForm({
                enable_param_lu_name: result === "true"
              });
              _context2.next = 9;
              break;
            case 7:
              _context2.prev = 7;
              _context2.t0 = _context2["catch"](0);
            case 9:
            case "end":
              return _context2.stop();
          }
        }, _callee2, null, [[0, 7]]);
      }));
      return _fetchEnableParamsLUName.apply(this, arguments);
    }
    function fetchEnableAdvancedSystemsForTesters() {
      return _fetchEnableAdvancedSystemsForTesters.apply(this, arguments);
    }
    function _fetchEnableAdvancedSystemsForTesters() {
      _fetchEnableAdvancedSystemsForTesters = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee3() {
        var result;
        return regenerator_default.a.wrap(function _callee3$(_context3) {
          while (1) switch (_context3.prev = _context3.next) {
            case 0:
              _context3.prev = 0;
              _context3.next = 3;
              return apis_task.getTaskLuEditForTesters();
            case 3:
              result = _context3.sent;
              saveForm({
                enable_advanced_for_testers: result === "true"
              });
              _context3.next = 9;
              break;
            case 7:
              _context3.prev = 7;
              _context3.t0 = _context3["catch"](0);
            case 9:
            case "end":
              return _context3.stop();
          }
        }, _callee3, null, [[0, 7]]);
      }));
      return _fetchEnableAdvancedSystemsForTesters.apply(this, arguments);
    }
    fetchEnableParamsLUName();
    fetchEnableAdvancedSystemsForTesters();
  }, []);
  var _useState = Object(react["useState"])(8),
    _useState2 = slicedToArray_default()(_useState, 2),
    fetchCounter = _useState2[0],
    setFetchCounter = _useState2[1];
  var _useState3 = Object(react["useState"])(false),
    _useState4 = slicedToArray_default()(_useState3, 2),
    finished = _useState4[0],
    setFinished = _useState4[1];
  Object(react["useEffect"])(function () {
    function fetchEnableParamWidth() {
      return _fetchEnableParamWidth.apply(this, arguments);
    }
    function _fetchEnableParamWidth() {
      _fetchEnableParamWidth = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee4() {
        var result;
        return regenerator_default.a.wrap(function _callee4$(_context4) {
          while (1) switch (_context4.prev = _context4.next) {
            case 0:
              _context4.prev = 0;
              _context4.next = 3;
              return apis_task.getEnableParamWidth();
            case 3:
              result = _context4.sent;
              saveForm({
                enable_param_auto_width: result === "true"
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context4.next = 10;
              break;
            case 8:
              _context4.prev = 8;
              _context4.t0 = _context4["catch"](0);
            case 10:
            case "end":
              return _context4.stop();
          }
        }, _callee4, null, [[0, 8]]);
      }));
      return _fetchEnableParamWidth.apply(this, arguments);
    }
    function fetchParamCoupling() {
      return _fetchParamCoupling.apply(this, arguments);
    }
    function _fetchParamCoupling() {
      _fetchParamCoupling = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee5() {
        var coupling_result;
        return regenerator_default.a.wrap(function _callee5$(_context5) {
          while (1) switch (_context5.prev = _context5.next) {
            case 0:
              _context5.prev = 0;
              _context5.next = 3;
              return apis_task.getCheckIfParamsCoupling();
            case 3:
              coupling_result = _context5.sent;
              saveForm({
                isCoupling: coupling_result === "true"
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context5.next = 10;
              break;
            case 8:
              _context5.prev = 8;
              _context5.t0 = _context5["catch"](0);
            case 10:
            case "end":
              return _context5.stop();
          }
        }, _callee5, null, [[0, 8]]);
      }));
      return _fetchParamCoupling.apply(this, arguments);
    }
    if (!taskData.task_id) {
      fetchEnableParamWidth();
      fetchParamCoupling();
      setFetchCounter(2);
      return;
    }
    var task_id = taskData.task_id;
    function fetchTaskPostExecutionProcess() {
      return _fetchTaskPostExecutionProcess.apply(this, arguments);
    }
    function _fetchTaskPostExecutionProcess() {
      _fetchTaskPostExecutionProcess = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee6() {
        var data;
        return regenerator_default.a.wrap(function _callee6$(_context6) {
          while (1) switch (_context6.prev = _context6.next) {
            case 0:
              _context6.prev = 0;
              _context6.next = 3;
              return apis_task.getTaskPostExecutionProcess(task_id);
            case 3:
              data = _context6.sent;
              saveForm({
                postExecutionProcesses: data.map(function (it) {
                  if (!it.parameters) {
                    it.parameters = {
                      inputs: []
                    };
                  } else {
                    it.parameters = JSON.parse(it.parameters);
                  }
                  it.editors = [];
                  return it;
                })
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context6.next = 10;
              break;
            case 8:
              _context6.prev = 8;
              _context6.t0 = _context6["catch"](0);
            case 10:
            case "end":
              return _context6.stop();
          }
        }, _callee6, null, [[0, 8]]);
      }));
      return _fetchTaskPostExecutionProcess.apply(this, arguments);
    }
    function fetchTaskVariables() {
      return _fetchTaskVariables.apply(this, arguments);
    }
    function _fetchTaskVariables() {
      _fetchTaskVariables = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee7() {
        var data;
        return regenerator_default.a.wrap(function _callee7$(_context7) {
          while (1) switch (_context7.prev = _context7.next) {
            case 0:
              _context7.prev = 0;
              _context7.next = 3;
              return apis_task.getTaskVariables(task_id);
            case 3:
              data = _context7.sent;
              saveForm({
                globals: data
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context7.next = 10;
              break;
            case 8:
              _context7.prev = 8;
              _context7.t0 = _context7["catch"](0);
            case 10:
            case "end":
              return _context7.stop();
          }
        }, _callee7, null, [[0, 8]]);
      }));
      return _fetchTaskVariables.apply(this, arguments);
    }
    function fetchTaskPreExecutionProcess() {
      return _fetchTaskPreExecutionProcess.apply(this, arguments);
    }
    function _fetchTaskPreExecutionProcess() {
      _fetchTaskPreExecutionProcess = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee8() {
        var data;
        return regenerator_default.a.wrap(function _callee8$(_context8) {
          while (1) switch (_context8.prev = _context8.next) {
            case 0:
              _context8.prev = 0;
              _context8.next = 3;
              return apis_task.getTaskPreExecutionProcess(task_id);
            case 3:
              data = _context8.sent;
              saveForm({
                preExecutionProcesses: data.map(function (it) {
                  if (!it.parameters) {
                    it.parameters = {
                      inputs: []
                    };
                  } else {
                    it.parameters = JSON.parse(it.parameters);
                  }
                  it.editors = [];
                  return it;
                })
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context8.next = 10;
              break;
            case 8:
              _context8.prev = 8;
              _context8.t0 = _context8["catch"](0);
            case 10:
            case "end":
              return _context8.stop();
          }
        }, _callee8, null, [[0, 8]]);
      }));
      return _fetchTaskPreExecutionProcess.apply(this, arguments);
    }
    function fetchTaskTables() {
      return _fetchTaskTables.apply(this, arguments);
    }
    function _fetchTaskTables() {
      _fetchTaskTables = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee9() {
        var data;
        return regenerator_default.a.wrap(function _callee9$(_context9) {
          while (1) switch (_context9.prev = _context9.next) {
            case 0:
              _context9.prev = 0;
              if (!(taskData.refcount === 0)) {
                _context9.next = 4;
                break;
              }
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              return _context9.abrupt("return");
            case 4:
              _context9.next = 6;
              return apis_task.getTaskTables(task_id);
            case 6:
              data = _context9.sent;
              saveForm({
                tableList: data.map(function (it) {
                  return useInit_objectSpread(useInit_objectSpread({}, it), {}, {
                    filter_parameters: it.filter_parameters ? it.filter_parameters.split("<#>") : it.filter_parameters,
                    reference_table_name: it.ref_table_name
                  });
                })
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context9.next = 13;
              break;
            case 11:
              _context9.prev = 11;
              _context9.t0 = _context9["catch"](0);
            case 13:
            case "end":
              return _context9.stop();
          }
        }, _callee9, null, [[0, 11]]);
      }));
      return _fetchTaskTables.apply(this, arguments);
    }
    function fetchSourceEnvironment() {
      return _fetchSourceEnvironment.apply(this, arguments);
    }
    function _fetchSourceEnvironment() {
      _fetchSourceEnvironment = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee10() {
        var data;
        return regenerator_default.a.wrap(function _callee10$(_context10) {
          while (1) switch (_context10.prev = _context10.next) {
            case 0:
              _context10.prev = 0;
              if (taskData.source_environment_id) {
                _context10.next = 4;
                break;
              }
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              return _context10.abrupt("return");
            case 4:
              _context10.next = 6;
              return apis_task.getEnvironmentByID(taskData.source_environment_id);
            case 6:
              data = _context10.sent;
              if (data && data[0]) {
                saveForm({
                  mask_sensitive_data: data[0].mask_sensitive_data
                });
              }
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context10.next = 13;
              break;
            case 11:
              _context10.prev = 11;
              _context10.t0 = _context10["catch"](0);
            case 13:
            case "end":
              return _context10.stop();
          }
        }, _callee10, null, [[0, 11]]);
      }));
      return _fetchSourceEnvironment.apply(this, arguments);
    }
    function fetchLogicalUntis() {
      return _fetchLogicalUntis.apply(this, arguments);
    }
    function _fetchLogicalUntis() {
      _fetchLogicalUntis = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee11() {
        var selectedData;
        return regenerator_default.a.wrap(function _callee11$(_context11) {
          while (1) switch (_context11.prev = _context11.next) {
            case 0:
              _context11.prev = 0;
              _context11.next = 3;
              return apis_task.getTaskLogicalUnits(taskData.task_id || 0);
            case 3:
              selectedData = _context11.sent;
              saveForm({
                selected_logical_units: selectedData.map(function (it) {
                  return it.lu_id;
                }),
                selected_logical_units_names: selectedData.map(function (it) {
                  return it.lu_name;
                })
              });
              setFetchCounter(function (prevCount) {
                return prevCount - 1;
              });
              _context11.next = 10;
              break;
            case 8:
              _context11.prev = 8;
              _context11.t0 = _context11["catch"](0);
            case 10:
            case "end":
              return _context11.stop();
          }
        }, _callee11, null, [[0, 8]]);
      }));
      return _fetchLogicalUntis.apply(this, arguments);
    }
    fetchTaskPostExecutionProcess();
    fetchTaskPreExecutionProcess();
    fetchTaskVariables();
    fetchTaskTables();
    fetchSourceEnvironment();
    fetchLogicalUntis();
    fetchEnableParamWidth();
    fetchParamCoupling();
  }, [taskData.task_id]);
  Object(react["useEffect"])(function () {
    if (fetchCounter === 0) {
      setFinished(true);
    }
  }, [fetchCounter]);
  return {
    initFinished: finished
  };
};
/* harmony default export */ var Main_useInit = (useInit_useInit);
// CONCATENATED MODULE: ./src/containers/Task/Main/useGenerationParams.ts




var useGenerationParams_useGenerationParams = function useGenerationParams(saveForm, dataSourceType, task_id, selected_logical_units_names, generateParams) {
  var getDataGenerationParams = Object(react["useCallback"])( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
    var data, updateData, selectedParams;
    return regenerator_default.a.wrap(function _callee$(_context) {
      while (1) switch (_context.prev = _context.next) {
        case 0:
          if (!(dataSourceType !== 'synthetic')) {
            _context.next = 2;
            break;
          }
          return _context.abrupt("return");
        case 2:
          console.log('getDataGenerationParams');
          _context.next = 5;
          return apis_task.getDataGenerationParams(task_id, selected_logical_units_names || []);
        case 5:
          data = _context.sent;
          updateData = {};
          selectedParams = [];
          Object.keys(data || {}).forEach(function (key) {
            var newValueAdded = false;
            if (generateParams && generateParams[key] && generateParams[key].value !== undefined) {
              data[key].editor.value = generateParams[key].value;
              data[key].value = generateParams[key].value;
              newValueAdded = true;
            } else {
              if (data[key].value !== undefined) {
                newValueAdded = true;
                data[key].editor.value = data[key].value;
                data[key].value = data[key].value;
              } else {
                data[key].editor.value = data[key].default;
              }
            }
            if (generateParams && generateParams[key] && generateParams[key].order) {
              data[key].order = generateParams[key].order;
            }
            if ((newValueAdded || data[key].mandatory) && data[key].order < 99999999) {
              selectedParams.push({
                key: key,
                order: data[key].order
              });
            }
          });
          updateData.generateChosenParams = selectedParams.sort(function (a, b) {
            return (a.order || 99999999) - (b.order || 99999999);
          }).map(function (it) {
            return it.key;
          });
          updateData.dataGenerationParams = data;
          saveForm(updateData);
        case 12:
        case "end":
          return _context.stop();
      }
    }, _callee);
  })), [saveForm, task_id, selected_logical_units_names, generateParams, dataSourceType]);
  Object(react["useEffect"])(function () {
    if (selected_logical_units_names && selected_logical_units_names.length > 0) {
      getDataGenerationParams();
    }
  }, [selected_logical_units_names]);

  // need to add code for tester
};
/* harmony default export */ var Main_useGenerationParams = (useGenerationParams_useGenerationParams);
// EXTERNAL MODULE: ./node_modules/sweetalert2/dist/sweetalert2.all.js
var sweetalert2_all = __webpack_require__(226);
var sweetalert2_all_default = /*#__PURE__*/__webpack_require__.n(sweetalert2_all);

// EXTERNAL MODULE: ./node_modules/sweetalert2-react-content/dist/sweetalert2-react-content.umd.js
var sweetalert2_react_content_umd = __webpack_require__(227);
var sweetalert2_react_content_umd_default = /*#__PURE__*/__webpack_require__.n(sweetalert2_react_content_umd);

// CONCATENATED MODULE: ./src/containers/Task/Main/useExecutionMode.ts


var useExecutionMode_useExecutionMode = function useExecutionMode(initFinished, taskData) {
  var toast = hooks_useToast();
  var clone_ind = taskData.clone_ind,
    execution_mode = taskData.execution_mode,
    be_execution_mode = taskData.be_execution_mode;
  Object(react["useEffect"])(function () {
    if (initFinished) {
      if (clone_ind && (execution_mode === 'VERTICAL' || execution_mode === 'INHERITED' && be_execution_mode === 'VERTICAL')) {
        toast.warning('The task execution will run in a horizontal mode since the entity clone does not support the vertical execution mode.');
      }
    }
  }, [clone_ind, execution_mode]);
};
/* harmony default export */ var Main_useExecutionMode = (useExecutionMode_useExecutionMode);
// CONCATENATED MODULE: ./src/containers/Task/Main/index.tsx





function Main_ownKeys(object, enumerableOnly) { var keys = Object.keys(object); if (Object.getOwnPropertySymbols) { var symbols = Object.getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return Object.getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }
function Main_objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? Main_ownKeys(Object(source), !0).forEach(function (key) { defineProperty_default()(target, key, source[key]); }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(target, Object.getOwnPropertyDescriptors(source)) : Main_ownKeys(Object(source)).forEach(function (key) { Object.defineProperty(target, key, Object.getOwnPropertyDescriptor(source, key)); }); } return target; }































var MySwal = sweetalert2_react_content_umd_default()(sweetalert2_all_default.a);
function TaskMain(props) {
  var _props$content = props.content,
    mode = _props$content.mode,
    task = _props$content.task,
    openTasks = _props$content.openTasks,
    copy = _props$content.copy,
    tasks = _props$content.tasks,
    scope = _props$content.scope;
  var toast = hooks_useToast();
  var _useState = Object(react["useState"])(utils_convertTaskData(task, copy)),
    _useState2 = slicedToArray_default()(_useState, 2),
    taskData = _useState2[0],
    setTaskData = _useState2[1];
  var _useState3 = Object(react["useState"])(false),
    _useState4 = slicedToArray_default()(_useState3, 2),
    saveInProgress = _useState4[0],
    setSaveInProgress = _useState4[1];
  var _useState5 = Object(react["useState"])(''),
    _useState6 = slicedToArray_default()(_useState5, 2),
    failedComp = _useState6[0],
    setFailedComp = _useState6[1];
  var _useState7 = Object(react["useState"])(false),
    _useState8 = slicedToArray_default()(_useState7, 2),
    disableChange = _useState8[0],
    setDisableChange = _useState8[1];
  var _useState9 = Object(react["useState"])(tasks && tasks.filter(function (it) {
      return it.task_status === 'Active';
    }).map(function (it) {
      return it.task_title || '';
    }) || []),
    _useState10 = slicedToArray_default()(_useState9, 2),
    task_titles = _useState10[0],
    setTaskTitles = _useState10[1];
  Object(react["useEffect"])(function () {
    var disableChangeLocal = false;
    if (taskData.task_id) {
      var authService = getService('AuthService');
      var username = (authService === null || authService === void 0 ? void 0 : authService.getUsername()) || '';
      var userRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
      if ((taskData === null || taskData === void 0 ? void 0 : taskData.task_status) === 'Inactive' || username !== (taskData === null || taskData === void 0 ? void 0 : taskData.task_created_by)) {
        disableChangeLocal = true;
      }
      if (taskData.task_status === 'Active' && (((taskData === null || taskData === void 0 ? void 0 : taskData.owners) || []).indexOf(username) >= 0 || (userRole === null || userRole === void 0 ? void 0 : userRole.type) === 'admin')) {
        disableChangeLocal = false;
      }
      if (copy) {
        disableChangeLocal = false;
      }
      setDisableChange(disableChangeLocal);
    }
  }, [taskData, copy]);
  Object(react["useEffect"])(function () {
    setTaskTitles(tasks && tasks.map(function (it) {
      return it.task_title || '';
    }) || []);
  }, [tasks]);
  Object(react["useEffect"])(function () {
    // if (!taskData.task_title) {
    if (!taskData.task_id) {
      var task_title = utils_getTaskTitle(taskData);
      saveForm({
        task_title: task_title
      });
    }
    // }
  }, [taskData.be_id, taskData.tables_selected, taskData.be_name]);
  Object(react["useEffect"])(function () {
    var BreadCrumbsService = getService('BreadCrumbsService');
    if (BreadCrumbsService) {
      BreadCrumbsService.push({
        task_id: taskData.task_title
      }, 'TASK_BREADCRUMB', function () {});
    }
  }, []);
  var _useForm = Object(index_esm["a" /* useForm */])({
      defaultValues: taskData
    }),
    resetField = _useForm.resetField,
    register = _useForm.register,
    unregister = _useForm.unregister,
    setValue = _useForm.setValue,
    handleSubmit = _useForm.handleSubmit,
    trigger = _useForm.trigger,
    _useForm$formState = _useForm.formState,
    errors = _useForm$formState.errors,
    isValid = _useForm$formState.isValid,
    clearErrors = _useForm.clearErrors;
  var _useWidgetStatus = Main_useWidgetStatus(taskData, trigger, isValid, handleSubmit, failedComp),
    statuses = _useWidgetStatus.statuses,
    onClickStep = _useWidgetStatus.onClickStep,
    currentStep = _useWidgetStatus.currentStep,
    touchedForms = _useWidgetStatus.touchedForms,
    setTouchedForms = _useWidgetStatus.setTouchedForms,
    submittedForm = _useWidgetStatus.submittedForm,
    setSubmittedForm = _useWidgetStatus.setSubmittedForm,
    statusesFuncMap = _useWidgetStatus.statusesFuncMap,
    initTask = _useWidgetStatus.initTask;
  var saveForm = Object(react["useCallback"])(function (data) {
    if (failedComp === currentStep) {
      setFailedComp('');
    }
    setTaskData(function (previousTaskData) {
      return Main_objectSpread(Main_objectSpread({}, previousTaskData), data);
    });
    Object.keys(data).forEach(function (key) {
      setValue(key, data[key]);
      clearErrors(key);
    });
  }, [setValue, clearErrors, setFailedComp, failedComp, currentStep]);
  var _useInit = Main_useInit(saveForm, taskData),
    initFinished = _useInit.initFinished;
  var allLogicalUnits = Main_useLogicalUnits(initFinished, saveForm, initTask, taskData === null || taskData === void 0 ? void 0 : taskData.dataSourceType, taskData === null || taskData === void 0 ? void 0 : taskData.source_type, taskData === null || taskData === void 0 ? void 0 : taskData.selected_logical_units_names, taskData === null || taskData === void 0 ? void 0 : taskData.be_type, taskData === null || taskData === void 0 ? void 0 : taskData.be_id, taskData === null || taskData === void 0 ? void 0 : taskData.source_environment_id, taskData === null || taskData === void 0 ? void 0 : taskData.environment_id);
  Main_usePeriods(saveForm, taskData.version_ind, taskData.dataSourceType, taskData.source_type, taskData.retention_period_value, taskData.retention_period_type, taskData.reserve_retention_period_value);
  Main_useExecutionMode(initFinished, taskData);
  Main_useRoles(saveForm, taskData);
  Main_useGenerationParams(saveForm, taskData.dataSourceType, taskData.task_id, taskData.selected_logical_units_names, taskData.dataGenerationParams);
  var onReset = Object(react["useCallback"])(function () {
    var authService = getService('AuthService');
    var systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
    if (currentStep === 'source_data_subset' || currentStep === 'target_data_subset') {
      if (taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables') {
        var tableList = (taskData.tableList || []).map(function (it) {
          it.gui_filter = undefined;
          it.filter_type = undefined;
          it.table_filter = undefined;
          return it;
        });
        saveForm({
          subsetReset: true,
          tableList: toConsumableArray_default()(tableList)
        });
      } else if (taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'generated_data') {
        saveForm({
          onReset: true,
          generation_type: 'all',
          selection_method: 'L',
          selection_param_value: undefined,
          num_of_entities: undefined,
          parameters: undefined,
          selected_subset_task_exe_id: undefined
        });
      } else {
        saveForm({
          generation_type: 'all',
          selection_method: 'L',
          selection_param_value: undefined,
          num_of_entities: undefined,
          parameters: undefined
        });
      }
    }
    if (currentStep === 'be_advanced') {
      saveForm({
        scheduler: 'immediate',
        globals: [],
        postExecutionProcesses: []
      });
    }
    // if (currentStep === 'be') {
    //   saveForm({
    //     be_id: null,
    //     be_name: '',
    //     selected_logical_units: [],
    //     tableList: [],
    //   })
    // }
    if (currentStep === 'source') {
      saveForm({
        dataSourceType: 'data_source',
        source_type: taskData.enable_masking_only ? 'tables' : 'BE',
        source_environment_id: null,
        source_environment_name: '',
        mask_sensitive_data: false,
        selected_training_name: '',
        selected_training_id: null,
        tables_selected: false,
        environment_id: taskData.be_type === 'source' ? undefined : taskData.environment_id,
        environment_name: taskData.be_type === 'source' ? undefined : taskData.environment_name,
        be_type: taskData.be_type === 'target' ? taskData.be_type : undefined,
        be_id: taskData.be_type === 'target' ? taskData.be_id : null,
        be_name: taskData.be_type === 'target' ? taskData.be_name : null,
        selected_logical_units: taskData.be_type === 'target' ? taskData.selected_logical_units : [],
        selected_logical_units_names: taskData.be_type === 'target' ? taskData.selected_logical_units_names : [],
        tableList: []
      });
    }
    if (currentStep === 'test_data_store') {
      var _taskData$userRole, _taskData$sourceUserR;
      var updateData = {
        retention_period_type: 'reset'
      };
      if (!(!((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || (!taskData.userRole || (_taskData$userRole = taskData.userRole) !== null && _taskData$userRole !== void 0 && _taskData$userRole.allowed_entity_versioning) && (!taskData.sourceUserRole || (_taskData$sourceUserR = taskData.sourceUserRole) !== null && _taskData$sourceUserR !== void 0 && _taskData$sourceUserR.allowed_entity_versioning) && (taskData.userRole || taskData.sourceUserRole)) || taskData.sync_mode === 'OFF' && taskData.dataSourceType === 'data_source' || taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'generated_data' || taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables' || taskData.retention_period_type === 'Do Not Retain')) {
        updateData.version_ind = false;
      }
      saveForm(updateData);
    }
    if (currentStep === 'task_title') {
      saveForm({
        task_description: '',
        task_title: taskData.task_id ? taskData.task_title : ''
      });
    }
    if (currentStep === 'target') {
      saveForm({
        environment_id: null,
        environment_name: '',
        target_env: 'target_env',
        load_entity: taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables' ? true : false,
        reserve_ind: false,
        delete_before_load: taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables' ? true : false,
        replace_sequences: false,
        reserve_note: '',
        clone_ind: false,
        source_environment_id: taskData.be_type === 'target' ? undefined : taskData.source_environment_id,
        source_environment_name: taskData.be_type === 'target' ? undefined : taskData.source_environment_name,
        be_type: taskData.be_type === 'source' ? taskData.be_type : undefined,
        be_id: taskData.be_type === 'source' ? taskData.be_id : taskData.be_id === -1 ? -1 : null,
        be_name: taskData.be_type === 'source' ? taskData.be_name : null,
        selected_logical_units: taskData.be_type === 'source' ? taskData.selected_logical_units : [],
        selected_logical_units_names: taskData.be_type === 'source' ? taskData.selected_logical_units_names : []
      });
    }
    clearErrors();
    if (currentStep && touchedForms.indexOf(currentStep) >= 0) {
      setTouchedForms(function (oldArray) {
        return oldArray.filter(function (it) {
          return it !== currentStep;
        });
      });
    }
    setTimeout(function () {
      saveForm({
        onReset: false
      });
    }, 500);
  }, [currentStep, saveForm, touchedForms, setTouchedForms, clearErrors, taskData]);

  // useEffect(() => {
  //   if (!taskData || (!taskData.be_id && (!taskData.tableList || taskData.tableList.length === 0))) {
  //     if (currentStep !== 'task_title' && currentStep !== 'scheduler'){
  //       onClickStep('');
  //     }
  //   }
  // },[taskData, onClickStep, currentStep]);

  console.log(taskData);
  var getCurrentForm = Object(react["useCallback"])(function () {
    if (currentStep === 'task_title') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTitle, {
        tasks_titles_active: task_titles
      });
    }
    // if (currentStep === 'be') {
    //     return (<BusinessEntityForm logical_units={allLogicalUnits} />);
    // }
    else if (currentStep === 'source') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSourceSettings, {});
    } else if (currentStep === 'source_data_subset' || currentStep === 'target_data_subset') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset, {});
    } else if (currentStep === 'test_data_store') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Froms_TestDataStore, {});
    } else if (currentStep === 'target') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Target, {});
    } else if (currentStep === 'be_advanced') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced, {
        be_id: taskData.be_id,
        task_id: taskData.task_id,
        logical_units: []
      });
    } else if (currentStep === 'scheduler') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(Scheduler, {});
    }
  }, [currentStep, taskData, task_titles]);
  var getSourceName = Object(react["useCallback"])(function () {
    if ((taskData.reserve_ind || taskData.delete_before_load) && !taskData.load_entity) {
      if (taskData.dataSourceType === 'data_source') {
        return ' ';
      }
      return taskData.environment_name;
    }
    if (taskData.dataSourceType === 'data_source') {
      return taskData.source_environment_name;
    }
    if (taskData.dataSourceType === 'synthetic') {
      return 'Rules Generation';
    }
    if (taskData.dataSourceType === 'ai_generated') {
      return 'AI Generation';
    }
  }, [taskData]);
  var getSourceSubTitle = Object(react["useCallback"])(function () {
    if (taskData.dataSourceType === 'ai_generated') {
      return taskData.selected_training_name || ' ';
    } else if (taskData.dataSourceType === 'synthetic') {
      return ' ';
    }
    return '';
  }, [taskData]);
  var getTargetName = Object(react["useCallback"])(function () {
    if (taskData.target_env === 'ai_training') {
      return 'AI Training';
    }
    return taskData.environment_name;
  }, [taskData]);
  var moveToMadatoryForms = Object(react["useCallback"])(function () {
    var source = statusesFuncMap['dataSourceStatus'](taskData);
    var target = statusesFuncMap['targetStatus'](taskData);
    var subsetPosition = statusesFuncMap['subsetPosition'](taskData);
    var subsetStatus = statusesFuncMap['subsetStatus'](taskData);
    var moveTo = function moveTo(step) {
      // @ts-ignore
      setFailedComp(step);
      onClickStep(step);
    };
    var testDataStoreStatus = statusesFuncMap['testDataStoreStatus'](taskData);
    if (source !== StatusEnum.completed && source !== StatusEnum.disabled) {
      moveTo('source');
    } else if (subsetPosition !== SubsetPossition.undefined && subsetStatus !== StatusEnum.completed) {
      moveTo(subsetPosition === SubsetPossition.source ? 'source_data_subset' : 'target_data_subset');
    } else if (testDataStoreStatus !== StatusEnum.completed) {
      moveTo('test_data_store');
    } else if (subsetPosition === SubsetPossition.target && target !== StatusEnum.completed || target === StatusEnum.partial || target === StatusEnum.blink) {
      moveTo('target');
    } else if (!taskData.task_title) {
      moveTo('task_title');
    }
  }, [taskData, statusesFuncMap, onClickStep, setFailedComp]);
  var isValidTaskData = Object(react["useCallback"])(function () {
    console.log(failedComp);
    var source = statusesFuncMap['dataSourceStatus'](taskData);
    var target = statusesFuncMap['targetStatus'](taskData, statuses);
    var subsetPosition = statusesFuncMap['subsetPosition'](taskData);
    var subsetStatus = statusesFuncMap['subsetStatus'](taskData);
    var testDataStoreStatus = statusesFuncMap['testDataStoreStatus'](taskData);
    if (source !== StatusEnum.completed && source !== StatusEnum.disabled || target === StatusEnum.blink || target === StatusEnum.partial || testDataStoreStatus !== StatusEnum.completed || subsetPosition !== SubsetPossition.undefined && subsetStatus !== StatusEnum.completed || !taskData.task_title) {
      return false;
    }
    if ((taskData.maxToCopy || 9007199254740992) < (taskData.num_of_entities || 0)) {
      if (taskData.dataSourceType === 'ai_generated' && taskData.synthetic_type === 'new_data' || taskData.dataSourceType === 'synthetic' && taskData.synthetic_type === 'new_data') {
        onClickStep('source');
      } else {
        if (subsetPosition === SubsetPossition.target) {
          onClickStep('target_data_subset');
          setFailedComp('target_data_subset');
        } else if (subsetPosition === SubsetPossition.source) {
          onClickStep('source_data_subset');
          setFailedComp('source_data_subset');
        }
      }
      return false;
    }
    return true;
  }, [taskData, statusesFuncMap, statuses, failedComp, onClickStep]);
  var handleFormErrors = Object(react["useCallback"])( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee() {
    var fieldErrors;
    return regenerator_default.a.wrap(function _callee$(_context) {
      while (1) switch (_context.prev = _context.next) {
        case 0:
          fieldErrors = Object.keys(errors);
          if (!(fieldErrors.length > 0)) {
            _context.next = 7;
            break;
          }
          // @ts-ignore
          setFailedComp(errors[fieldErrors[0]].ref.step);
          // @ts-ignore
          onClickStep(errors[fieldErrors[0]].ref.step);
          return _context.abrupt("return", true);
        case 7:
          _context.next = 9;
          return setFailedComp('');
        case 9:
          return _context.abrupt("return", false);
        case 10:
        case "end":
          return _context.stop();
      }
    }, _callee);
  })), [onClickStep, errors, setFailedComp]);
  var saveTask = Object(react["useCallback"])( /*#__PURE__*/function () {
    var _ref2 = asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee2(openTask) {
      var formResult, result, checkProcesss, dataForSave, AuthService, role, _result, validateReserveEntitiesResult, _result2;
      return regenerator_default.a.wrap(function _callee2$(_context2) {
        while (1) switch (_context2.prev = _context2.next) {
          case 0:
            if (!saveInProgress) {
              _context2.next = 2;
              break;
            }
            return _context2.abrupt("return");
          case 2:
            _context2.next = 4;
            return setSubmittedForm(true);
          case 4:
            _context2.next = 6;
            return handleFormErrors();
          case 6:
            formResult = _context2.sent;
            if (!formResult) {
              _context2.next = 10;
              break;
            }
            toast.error("All mandatory information must be completed before saving the task");
            return _context2.abrupt("return", false);
          case 10:
            if (isValidTaskData()) {
              _context2.next = 14;
              break;
            }
            toast.error("All mandatory information must be completed before saving the task");
            moveToMadatoryForms();
            return _context2.abrupt("return", false);
          case 14:
            result = null;
            if (!(taskData.version_ind && taskData.selected_version_task_name)) {
              _context2.next = 20;
              break;
            }
            if (!((taskData.selected_version_succeeded_entities || 0) > (taskData.maxToCopy || 0))) {
              _context2.next = 20;
              break;
            }
            toast.error('The number of entities exceeds the number of entities in the read write permission');
            onClickStep('target_data_subset');
            return _context2.abrupt("return", false);
          case 20:
            checkProcesss = function checkProcesss(data) {
              for (var i = 0; i < data.length; i++) {
                var processData = data[0];
                if (processData.editors.findIndex(function (it) {
                  return it.mandatory && (it.value === undefined || it.value === null);
                }) >= 0) {
                  return true;
                }
              }
              return false;
            };
            if (!(taskData.preExecutionProcesses && taskData.preExecutionProcesses.length > 0 && checkProcesss(taskData.preExecutionProcesses))) {
              _context2.next = 25;
              break;
            }
            toast.error('Mandatory info is missing for the pre-execution process');
            onClickStep('be_advanced');
            return _context2.abrupt("return", false);
          case 25:
            if (!(taskData.postExecutionProcesses && taskData.postExecutionProcesses.length > 0 && checkProcesss(taskData.postExecutionProcesses))) {
              _context2.next = 29;
              break;
            }
            toast.error('Mandatory info is missing for the post-execution process');
            onClickStep('be_advanced');
            return _context2.abrupt("return", false);
          case 29:
            dataForSave = prepareDataForSave(taskData, allLogicalUnits, copy);
            if (true) {
              _context2.next = 33;
              break;
            }
            console.log(dataForSave);
            return _context2.abrupt("return");
          case 33:
            setSaveInProgress(true);
            _context2.prev = 34;
            AuthService = getService('AuthService');
            role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
            if (!(role && role.type === 'tester' && dataForSave.version_ind && dataForSave.retention_period_type === 'Do Not Delete')) {
              _context2.next = 44;
              break;
            }
            _context2.next = 40;
            return MySwal.fire({
              title: /*#__PURE__*/Object(jsx_runtime["jsx"])("h2", {
                style: {
                  color: '#575757',
                  fontSize: '30px',
                  textAlign: 'center',
                  fontWeight: 600,
                  textTransform: 'none',
                  position: 'relative',
                  margin: '25px 0',
                  padding: 0,
                  lineHeight: '40px',
                  display: 'block'
                },
                children: "The tester is not permitted to execute tasks with unlimited retention period (Do not delete). Are you sure you wish to save the task?"
              }),
              showCancelButton: true,
              icon: "warning",
              confirmButtonText: "Yes",
              cancelButtonText: "No"
            }).then(function (value) {
              console.log(value);
              return value.isConfirmed;
            }).catch(function () {
              return false;
            });
          case 40:
            _result = _context2.sent;
            if (_result) {
              _context2.next = 44;
              break;
            }
            setSaveInProgress(false);
            return _context2.abrupt("return");
          case 44:
            if (!(dataForSave.task_type !== 'EXTRACT' && dataForSave.selection_method === 'L' && !dataForSave.replace_sequences && dataForSave.filterout_reserved !== 'NA')) {
              _context2.next = 56;
              break;
            }
            _context2.next = 47;
            return apis_task.validateReservedEntitiesList(dataForSave.be_id, dataForSave.environment_id, dataForSave.selection_param_value.split(',').map(function (it) {
              return {
                target_entity_id: it
              };
            }), dataForSave.filterout_reserved);
          case 47:
            validateReserveEntitiesResult = _context2.sent;
            if (!(validateReserveEntitiesResult.length > 0)) {
              _context2.next = 56;
              break;
            }
            _context2.next = 51;
            return MySwal.fire({
              title: /*#__PURE__*/Object(jsx_runtime["jsx"])("h2", {
                style: {
                  color: '#575757',
                  fontSize: '30px',
                  textAlign: 'center',
                  fontWeight: 600,
                  textTransform: 'none',
                  position: 'relative',
                  margin: '25px 0',
                  padding: 0,
                  lineHeight: '40px',
                  display: 'block'
                },
                children: "The task contains reserved entities. Are you sure you want to save the task?"
              }),
              showCancelButton: true,
              icon: "warning",
              confirmButtonText: "Yes",
              cancelButtonText: "No"
            }).then(function (value) {
              console.log(value);
              return value.isConfirmed;
            }).catch(function () {
              return false;
            });
          case 51:
            _result2 = _context2.sent;
            console.log(_result2);
            if (_result2) {
              _context2.next = 56;
              break;
            }
            setSaveInProgress(false);
            return _context2.abrupt("return");
          case 56:
            _context2.next = 58;
            return apis_task.saveTaskAPI(dataForSave);
          case 58:
            result = _context2.sent;
            toast.success("Task # ".concat(taskData.task_title, " Is Updated Successfully"));
            if (!openTask) {
              openTasks(true);
            }
            _context2.next = 67;
            break;
          case 63:
            _context2.prev = 63;
            _context2.t0 = _context2["catch"](34);
            console.log(_context2.t0);
            toast.error("Task # ".concat(taskData.task_id || '', " Failed to Update : ").concat(_context2.t0.message));
          case 67:
            _context2.prev = 67;
            setSaveInProgress(false);
            return _context2.finish(67);
          case 70:
            return _context2.abrupt("return", result);
          case 71:
          case "end":
            return _context2.stop();
        }
      }, _callee2, null, [[34, 63, 67, 70]]);
    }));
    return function (_x) {
      return _ref2.apply(this, arguments);
    };
  }(), [taskData, saveInProgress, handleFormErrors, openTasks, copy, setSaveInProgress, allLogicalUnits, toast, moveToMadatoryForms, setSubmittedForm, isValidTaskData]);
  var saveAndExecute = Object(react["useCallback"])( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee3() {
    var result;
    return regenerator_default.a.wrap(function _callee3$(_context3) {
      while (1) switch (_context3.prev = _context3.next) {
        case 0:
          _context3.next = 2;
          return saveTask(true);
        case 2:
          result = _context3.sent;
          if (result) {
            openTasks(true, result === null || result === void 0 ? void 0 : result.id);
          }
        case 4:
        case "end":
          return _context3.stop();
      }
    }, _callee3);
  })), [saveTask, openTasks]);
  var _useState11 = Object(react["useState"])({
      execute: false,
      trigger: false
    }),
    _useState12 = slicedToArray_default()(_useState11, 2),
    triggerSave = _useState12[0],
    setTriggerSave = _useState12[1];
  Object(react["useEffect"])(function () {
    if (triggerSave && triggerSave.trigger) {
      handleSubmit(function () {
        if (triggerSave.execute) {
          saveAndExecute();
        } else {
          saveTask();
        }
      }, function (data) {
        var fields = Object.keys(data);
        if (fields && fields.length > 0) {
          if (fields[0] === 'task_title') {
            onClickStep('task_title');
          } else {
            onClickStep(data[fields[0]].ref.step);
          }
        }
        ;
        console.log(data);
        toast.error("All mandatory information must be completed before saving the task");
      })();
    }
  }, [triggerSave]);
  var preSaveTask = Object(react["useCallback"])(function (execute) {
    setFailedComp('');
    setTriggerSave({
      execute: execute || false,
      trigger: true
    });
  }, [setTriggerSave, setFailedComp]);
  var deleteTask = Object(react["useCallback"])( /*#__PURE__*/asyncToGenerator_default()( /*#__PURE__*/regenerator_default.a.mark(function _callee4() {
    return regenerator_default.a.wrap(function _callee4$(_context4) {
      while (1) switch (_context4.prev = _context4.next) {
        case 0:
          if (!(!taskData.task_id || !taskData.task_title)) {
            _context4.next = 2;
            break;
          }
          return _context4.abrupt("return");
        case 2:
          _context4.next = 4;
          return apis_task.deleteTask(taskData.task_id, taskData.task_title);
        case 4:
          openTasks(true);
        case 5:
        case "end":
          return _context4.stop();
      }
    }, _callee4);
  })), [taskData, openTasks]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Container, {
    className: "react-comp",
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(task_TaskActions, {
      setCurrentStep: onClickStep,
      task_title: taskData.task_title,
      saveLocalData: saveForm,
      task_id: taskData.task_id,
      be_name: taskData.be_name,
      tables_selected: taskData.tables_selected,
      saveTask: function saveTask() {
        return preSaveTask();
      },
      closeTask: openTasks,
      saveAndExecute: function saveAndExecute() {
        return preSaveTask(true);
      },
      deleteTask: deleteTask,
      disableChange: disableChange,
      task_execution_status: taskData.task_execution_status
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(WidgetWrapper, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(WidgetContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_TaskMainWidget, {
          selectedStep: currentStep,
          onClick: onClickStep,
          data: statuses,
          sourceInfo: getSourceInfo(taskData),
          targetInfo: getTargetInfo(taskData),
          subsetInfo: utils_getSubsetInfo(taskData),
          datastoreInfo: utils_getTestDataStoreInfo(taskData, statusesFuncMap['testDataStoreStatus'](taskData), statusesFuncMap['subsetStatus'](taskData), statusesFuncMap['subsetPosition'](taskData)),
          environment_name: getTargetName(),
          source_environment_name: getSourceName(),
          sourceSubTitle: getSourceSubTitle(),
          targetSubTitle: taskData.environment_name ? '' : ' '
        })
      })
    }), currentStep ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskContext.Provider, {
      value: {
        resetField: resetField,
        unregister: unregister,
        register: register,
        clearErrors: clearErrors,
        errors: errors,
        submittedForm: submittedForm,
        saveForm: saveForm,
        taskData: taskData,
        allLogicalUnits: allLogicalUnits,
        statusesFuncMap: statusesFuncMap,
        scope: scope,
        copy: copy || false,
        config_params: {
          enable_param_auto_width: taskData.enable_param_auto_width
        }
      },
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_TaskForm, Main_objectSpread(Main_objectSpread({}, stepsConfig[currentStep]), {}, {
        onReset: onReset,
        width: stepsConfig[currentStep].width || 1590,
        children: getCurrentForm()
      }))
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var Main = (TaskMain);
// CONCATENATED MODULE: ./src/utils/ExportToNg.jsx




var ExportToNg_TaskMainComp = function TaskMainComp(props) {
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(react_default.a.StrictMode, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Main, {
      content: props.content
    })
  });
};
react_to_angular(ExportToNg_TaskMainComp, "reactTaskMain",
// eslint-disable-next-line no-undef
angular.module("react-connector", []), {
  content: '='
});

/***/ })

/******/ });
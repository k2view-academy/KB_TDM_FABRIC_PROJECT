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
/******/ 	deferredModules.push([226,1]);
/******/ 	// run deferred modules when ready
/******/ 	return checkDeferredModules();
/******/ })
/************************************************************************/
/******/ ({

/***/ 226:
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(320);


/***/ }),

/***/ 266:
/***/ (function(module, exports, __webpack_require__) {

var map = {
	"./af": 81,
	"./af.js": 81,
	"./ar": 82,
	"./ar-dz": 83,
	"./ar-dz.js": 83,
	"./ar-kw": 84,
	"./ar-kw.js": 84,
	"./ar-ly": 85,
	"./ar-ly.js": 85,
	"./ar-ma": 86,
	"./ar-ma.js": 86,
	"./ar-sa": 87,
	"./ar-sa.js": 87,
	"./ar-tn": 88,
	"./ar-tn.js": 88,
	"./ar.js": 82,
	"./az": 89,
	"./az.js": 89,
	"./be": 90,
	"./be.js": 90,
	"./bg": 91,
	"./bg.js": 91,
	"./bm": 92,
	"./bm.js": 92,
	"./bn": 93,
	"./bn-bd": 94,
	"./bn-bd.js": 94,
	"./bn.js": 93,
	"./bo": 95,
	"./bo.js": 95,
	"./br": 96,
	"./br.js": 96,
	"./bs": 97,
	"./bs.js": 97,
	"./ca": 98,
	"./ca.js": 98,
	"./cs": 99,
	"./cs.js": 99,
	"./cv": 100,
	"./cv.js": 100,
	"./cy": 101,
	"./cy.js": 101,
	"./da": 102,
	"./da.js": 102,
	"./de": 103,
	"./de-at": 104,
	"./de-at.js": 104,
	"./de-ch": 105,
	"./de-ch.js": 105,
	"./de.js": 103,
	"./dv": 106,
	"./dv.js": 106,
	"./el": 107,
	"./el.js": 107,
	"./en-au": 108,
	"./en-au.js": 108,
	"./en-ca": 109,
	"./en-ca.js": 109,
	"./en-gb": 110,
	"./en-gb.js": 110,
	"./en-ie": 111,
	"./en-ie.js": 111,
	"./en-il": 112,
	"./en-il.js": 112,
	"./en-in": 113,
	"./en-in.js": 113,
	"./en-nz": 114,
	"./en-nz.js": 114,
	"./en-sg": 115,
	"./en-sg.js": 115,
	"./eo": 116,
	"./eo.js": 116,
	"./es": 117,
	"./es-do": 118,
	"./es-do.js": 118,
	"./es-mx": 119,
	"./es-mx.js": 119,
	"./es-us": 120,
	"./es-us.js": 120,
	"./es.js": 117,
	"./et": 121,
	"./et.js": 121,
	"./eu": 122,
	"./eu.js": 122,
	"./fa": 123,
	"./fa.js": 123,
	"./fi": 124,
	"./fi.js": 124,
	"./fil": 125,
	"./fil.js": 125,
	"./fo": 126,
	"./fo.js": 126,
	"./fr": 127,
	"./fr-ca": 128,
	"./fr-ca.js": 128,
	"./fr-ch": 129,
	"./fr-ch.js": 129,
	"./fr.js": 127,
	"./fy": 130,
	"./fy.js": 130,
	"./ga": 131,
	"./ga.js": 131,
	"./gd": 132,
	"./gd.js": 132,
	"./gl": 133,
	"./gl.js": 133,
	"./gom-deva": 134,
	"./gom-deva.js": 134,
	"./gom-latn": 135,
	"./gom-latn.js": 135,
	"./gu": 136,
	"./gu.js": 136,
	"./he": 137,
	"./he.js": 137,
	"./hi": 138,
	"./hi.js": 138,
	"./hr": 139,
	"./hr.js": 139,
	"./hu": 140,
	"./hu.js": 140,
	"./hy-am": 141,
	"./hy-am.js": 141,
	"./id": 142,
	"./id.js": 142,
	"./is": 143,
	"./is.js": 143,
	"./it": 144,
	"./it-ch": 145,
	"./it-ch.js": 145,
	"./it.js": 144,
	"./ja": 146,
	"./ja.js": 146,
	"./jv": 147,
	"./jv.js": 147,
	"./ka": 148,
	"./ka.js": 148,
	"./kk": 149,
	"./kk.js": 149,
	"./km": 150,
	"./km.js": 150,
	"./kn": 151,
	"./kn.js": 151,
	"./ko": 152,
	"./ko.js": 152,
	"./ku": 153,
	"./ku.js": 153,
	"./ky": 154,
	"./ky.js": 154,
	"./lb": 155,
	"./lb.js": 155,
	"./lo": 156,
	"./lo.js": 156,
	"./lt": 157,
	"./lt.js": 157,
	"./lv": 158,
	"./lv.js": 158,
	"./me": 159,
	"./me.js": 159,
	"./mi": 160,
	"./mi.js": 160,
	"./mk": 161,
	"./mk.js": 161,
	"./ml": 162,
	"./ml.js": 162,
	"./mn": 163,
	"./mn.js": 163,
	"./mr": 164,
	"./mr.js": 164,
	"./ms": 165,
	"./ms-my": 166,
	"./ms-my.js": 166,
	"./ms.js": 165,
	"./mt": 167,
	"./mt.js": 167,
	"./my": 168,
	"./my.js": 168,
	"./nb": 169,
	"./nb.js": 169,
	"./ne": 170,
	"./ne.js": 170,
	"./nl": 171,
	"./nl-be": 172,
	"./nl-be.js": 172,
	"./nl.js": 171,
	"./nn": 173,
	"./nn.js": 173,
	"./oc-lnc": 174,
	"./oc-lnc.js": 174,
	"./pa-in": 175,
	"./pa-in.js": 175,
	"./pl": 176,
	"./pl.js": 176,
	"./pt": 177,
	"./pt-br": 178,
	"./pt-br.js": 178,
	"./pt.js": 177,
	"./ro": 179,
	"./ro.js": 179,
	"./ru": 180,
	"./ru.js": 180,
	"./sd": 181,
	"./sd.js": 181,
	"./se": 182,
	"./se.js": 182,
	"./si": 183,
	"./si.js": 183,
	"./sk": 184,
	"./sk.js": 184,
	"./sl": 185,
	"./sl.js": 185,
	"./sq": 186,
	"./sq.js": 186,
	"./sr": 187,
	"./sr-cyrl": 188,
	"./sr-cyrl.js": 188,
	"./sr.js": 187,
	"./ss": 189,
	"./ss.js": 189,
	"./sv": 190,
	"./sv.js": 190,
	"./sw": 191,
	"./sw.js": 191,
	"./ta": 192,
	"./ta.js": 192,
	"./te": 193,
	"./te.js": 193,
	"./tet": 194,
	"./tet.js": 194,
	"./tg": 195,
	"./tg.js": 195,
	"./th": 196,
	"./th.js": 196,
	"./tk": 197,
	"./tk.js": 197,
	"./tl-ph": 198,
	"./tl-ph.js": 198,
	"./tlh": 199,
	"./tlh.js": 199,
	"./tr": 200,
	"./tr.js": 200,
	"./tzl": 201,
	"./tzl.js": 201,
	"./tzm": 202,
	"./tzm-latn": 203,
	"./tzm-latn.js": 203,
	"./tzm.js": 202,
	"./ug-cn": 204,
	"./ug-cn.js": 204,
	"./uk": 205,
	"./uk.js": 205,
	"./ur": 206,
	"./ur.js": 206,
	"./uz": 207,
	"./uz-latn": 208,
	"./uz-latn.js": 208,
	"./uz.js": 207,
	"./vi": 209,
	"./vi.js": 209,
	"./x-pseudo": 210,
	"./x-pseudo.js": 210,
	"./yo": 211,
	"./yo.js": 211,
	"./zh-cn": 212,
	"./zh-cn.js": 212,
	"./zh-hk": 213,
	"./zh-hk.js": 213,
	"./zh-mo": 214,
	"./zh-mo.js": 214,
	"./zh-tw": 215,
	"./zh-tw.js": 215
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
webpackContext.id = 266;

/***/ }),

/***/ 320:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
// ESM COMPAT FLAG
__webpack_require__.r(__webpack_exports__);

// EXTERNAL MODULE: ./node_modules/react/index.js
var react = __webpack_require__(1);
var react_default = /*#__PURE__*/__webpack_require__.n(react);

// EXTERNAL MODULE: ./node_modules/styled-components/dist/styled-components.browser.esm.js
var styled_components_browser_esm = __webpack_require__(2);

// CONCATENATED MODULE: ./src/containers/Task/Main/styles.ts

const Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    min-height: calc(100vh - 42px);
    background-color: white;
    position: relative;
`;
const WidgetWrapper = styled_components_browser_esm["b" /* default */].div`
    width: calc(100vw - 24px);
    height: 280px;
    background-color: #f2f2f2;
`;
const WidgetContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    height: 277px;
    display: flex;
    justify-content: center;
`;
const FormContainer = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    width: ${props => props.width}px;
    left: calc(100% - ${props => props.width / 2}px;
    height: auto;
    background-color: #fff;
`;
// EXTERNAL MODULE: ./node_modules/react-hook-form/dist/index.esm.mjs
var index_esm = __webpack_require__(20);

// CONCATENATED MODULE: ./src/components/task/TaskForm/styles.ts

const Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100vw;
    height: auto;
    display: flex;
    justify-content: center;
`;

// min-width: ${(props) => props.width - 42 - 50}px;
const InnerWrapper = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    min-width: 80vw;
    max-width: 95vw;
    padding-bottom: 90px;
    top: 214px;
`;
const styles_Container = styled_components_browser_esm["b" /* default */].div`
    background-color: #fff;
    border-radius: 6px;
    box-shadow: 0 0 21px 0 rgba(51, 51, 51, 0.2);
`;
const TitleContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    justify-content: center;
    width: auto;
    position: relative;
    margin: auto;
`;
const Title = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: flex-start;
    font-family: Roboto;
    font-size: 20px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: center;
    color: var(--secondary-color);
    margin-bottom: 12px;
`;
const Body = styled_components_browser_esm["b" /* default */].div`
    margin-top: 15px;
    position: relative;
    padding: 0px 44px 44px 50px;
`;
const ResetButton = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    right: 0px;
    top: 0px;
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1683f2;
    display: flex;
    gap: 6px;
    align-items: center;
    cursor: pointer;
`;
const CloseButton = styled_components_browser_esm["b" /* default */].div`
    bottom: -107px;
    left: ${props => props.width / 2 - 27}px;
    position: absolute;
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background-color: #1483f3;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
`;
const styles_Icon = styled_components_browser_esm["b" /* default */].img`
`;
const Form = styled_components_browser_esm["b" /* default */].form`
`;
const DashedContainer = styled_components_browser_esm["b" /* default */].div`
    height: 5px;
    width: 100%;

`;
const StickyHeader = styled_components_browser_esm["b" /* default */].div`
    position: sticky;
    top: 0;
    padding: 20px 44px 0px 50px;
    z-index: 100;
    background-color: white;
`;
const RegularContainer = styled_components_browser_esm["b" /* default */].div`
    height: 3px;
    width: 100%;
    background-image: ${props => {
  if (props.dashed) {
    return `linear-gradient(90deg, ${props.color1} 50%, transparent 50%)`;
  }
  return `linear-gradient(to right, ${props.color1},  ${props.color2})`;
}};
    background-size: ${props => props.dashed ? '30px 10px,40px 10px,40px 10px,40px 10px' : ''};
    position: absolute;
    bottom: 0px;

`;
const SaveFormButton = styled_components_browser_esm["b" /* default */].button`
    display: none;
`;
const MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span`
    color: red;
`;
// CONCATENATED MODULE: ./src/images/revert-icon.svg
/* harmony default export */ var revert_icon = ("js/dist/7ce9dc66c632361100775a2c723eb394.svg");
// EXTERNAL MODULE: ./node_modules/react/jsx-runtime.js
var jsx_runtime = __webpack_require__(0);

// CONCATENATED MODULE: ./src/components/task/TaskForm/HeaderTitleBorder.tsx


function HeaderTitleBorder(props) {
  const {
    dashed,
    color1,
    color2
  } = props;
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(RegularContainer, {
    dashed: dashed,
    color1: color1,
    color2: color2
  });
}
/* harmony default export */ var TaskForm_HeaderTitleBorder = (HeaderTitleBorder);
// CONCATENATED MODULE: ./src/components/task/TaskForm/index.tsx




function TaskForm(props) {
  const {
    title,
    width,
    hideReset,
    children,
    mandatory,
    onReset,
    dashed,
    title_border_color,
    title_border_color2
  } = props;
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
              children: ["Clear form", /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Icon, {
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
const stepsConfig = {
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
const taskTypeHints = {
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

const styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
`;
const DataSourceTypes = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding-bottom: 30px;
    margin-top: 10px;
    border-bottom: solid 1px #ccc;
    width: 100%;
`;
const MaskDataContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 13px;
    align-items: center;
    margin-top: 27px;
    align-self: center;
    min-width: 203px;
`;
const DataSourceTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
`;
const DataSourceSettings_styles_Icon = styled_components_browser_esm["b" /* default */].img`

`;
const EnvironmentsContainer = styled_components_browser_esm["b" /* default */].div`
    display: ${props => props.data_source ? 'flex' : 'none'};
    align-items: flex-start;
    flex-direction: column;
    width: 560px;
    gap: 10px;

`;
const FetchDataPolicyContainer = styled_components_browser_esm["b" /* default */].div`
    margin-top: 10px;
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;
`;
const styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    color: #2e2e2e;
`;
const TitleBold = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    color: #2e2e2e;
`;
const SyntheticContainer = styled_components_browser_esm["b" /* default */].div`
    border-left: ${props => props.widthBorder ? ' #ccc solid 1px' : ''};
    display: flex;
    gap: 30px;
    align-items: center;
    padding: ${props => props.widthBorder ? ' 0px 0px 0px 20px' : ''} ;
`;
const SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div`
    align-self: flex-start;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const EnvironmentAndMaskData = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 40px;
`;
const DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div`
    margin-bottom: 10px;
`;
const DataSourceContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 25px;
    width: 100%;
`;
// CONCATENATED MODULE: ./src/components/radio/styles.ts

const radio_styles_Container = styled_components_browser_esm["b" /* default */].label`
    display: flex;
    align-items: center;
    cursor: pointer;
`;
const radio_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    text-align: center;
    color: #2e2e2e;
`;
const RadioInput = styled_components_browser_esm["b" /* default */].input`
    margin: 0px 5px 0px 0px !important;
    width: 20px;
    height: 20px;
`;
const radio_styles_Icon = styled_components_browser_esm["b" /* default */].img`

`;
// CONCATENATED MODULE: ./src/components/radio/index.tsx



function Radio(props) {
  const {
    title,
    name,
    value,
    onChange,
    selectedValue,
    disabled,
    tooltip
  } = props;
  const onChangeLocal = Object(react["useCallback"])(event => {
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

const DataGenerationParameters_styles_Container = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 30px;
    position: relative;
    width: 100%;
`;
const ParamsContainer = styled_components_browser_esm["b" /* default */].div`
    width: 350px;
`;
const ParamsList = styled_components_browser_esm["b" /* default */].ul`
    padding: 0;
    width: 100%;
    margin: 0;
    max-height: 235px;
    overflow: auto;
    max-width: 100%;
    border-radius: 3px;
    box-shadow: 0 0 9px 1px rgba(51, 51, 51, 0.2);
    border: solid 1px #ccc;
    background-color: #fff;

`;
const ParamsItem = styled_components_browser_esm["b" /* default */].li`
    max-height: 250px;
    overflow: auto;
    overflow-x: hidden;
    padding: 13px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: ${props => props.chosen ? '#f2f2f2' : 'transparent'};
`;
const ParamsItemText = styled_components_browser_esm["b" /* default */].span`
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    width: calc(100% - 16px);
`;
const DataGenerationParameters_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    width: 16px;
`;
const DummyIcon = styled_components_browser_esm["b" /* default */].img`
    padding-right: 16px;
`;
const Leftside = styled_components_browser_esm["b" /* default */].div`
    border-right: ${props => props.hideBorders ? '' : '1px solid #ccc'};
    display: flex;
    flex-direction: column;
    gap: 25px;
    padding-right: 30px;
`;
const Middle = styled_components_browser_esm["b" /* default */].div`
    border-right: ${props => props.hideBorders ? '' : '1px solid #ccc'};
    padding-right: 30px;
`;
const RightSide = styled_components_browser_esm["b" /* default */].div`
    flex: 1;
`;
const DummyImg = styled_components_browser_esm["b" /* default */].img`
`;
const styles_SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div`
    align-self: flex-start;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const Seprator = styled_components_browser_esm["b" /* default */].div`
    border-right: 1px solid #ccc;
    width: 1px;
    position: absolute;
    height: calc(100% + 110px);
    top: -30px;
    left: 400px;
`;
const PopoverTemplate = styled_components_browser_esm["b" /* default */].div`
    padding: 10px;
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    width: max-content;
    max-width: 250px;
    max-height: 400px;
    border-radius:3px;
      border: solid 1px #ccc;
      background-color: #fff;
`;
const FieldDescription = styled_components_browser_esm["b" /* default */].div`
`;
const styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div`
    border-bottom:  ${props => props.hideBorders ? '' : '1px solid #ccc'};
    padding-bottom: 10px;
`;
// CONCATENATED MODULE: ./src/components/Input/styles.ts

const Input_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: ${props => props.width || '100%'};
    position: relative;
`;
const Input_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 7px;
`;
const styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span`
    color: red;
`;
const ErrorContainer = styled_components_browser_esm["b" /* default */].small`
    color: #ed5565;
`;
const Input = styled_components_browser_esm["b" /* default */].input`
    height: unset;
    font-family: Roboto;
    font-size: 15px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
    padding: ${props => props.small ? '5px' : '9px'} 10px;
    border-radius: 3px;
    border: solid 1px #ccc;
    width: -webkit-fill-available;
    width: -moz-available;
    &:placeholder{
        font-size: 15px;
        font-weight: normal;
        font-stretch: normal;
        font-style: normal;
        line-height: 1.33;
        letter-spacing: normal;
        text-align: left;
        color: #999;
    }
`;
// CONCATENATED MODULE: ./src/containers/Task/Main/TaskContext.ts

const TaskContext = /*#__PURE__*/Object(react["createContext"])({
  resetField: null,
  unregister: null,
  register: null,
  clearErrors: null,
  errors: null,
  submittedForm: null,
  saveForm: () => {},
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

const FieldError_styles_Container = styled_components_browser_esm["b" /* default */].small`
    display: ${props => props.visible ? 'block' : 'none'};
    color: ${props => props.isInfo ? '#2e2e2e' : '#ed5565'};
    position: ${props => props.position ? 'relative' : 'absolute'};
    bottom: ${props => props.position ? '' : '-18px'};
    left: ${props => props.position ? '' : '2px'};
    font-size: ${props => props.isInfo ? '15px' : ''}; 
    font-weight: ${props => props.isInfo ? '500' : ''};
    white-space: ${props => props.width ? 'pre-line' : 'nowrap'};
`;
// CONCATENATED MODULE: ./src/components/FieldError/index.tsx




function FieldError(props) {
  const {
    submittedForm
  } = Object(react["useContext"])(TaskContext);
  const {
    error,
    submit,
    relativePosition,
    info,
    width
  } = props;
  let visible = false;
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




let InputTypes = /*#__PURE__*/function (InputTypes) {
  InputTypes["number"] = "number";
  InputTypes["text"] = "text";
  return InputTypes;
}({});
function TDMInput(props) {
  const {
    title,
    value,
    onChange,
    name,
    type,
    mandatory,
    placeholder,
    width,
    min,
    max,
    error,
    disabled,
    small
  } = props;
  const onChangeLocal = Object(react["useCallback"])(event => {
    if (type === 'number') {
      onChange(isNaN(event.target.valueAsNumber) ? undefined : event.target.valueAsNumber);
    } else {
      onChange(event.target.value);
    }
  }, [onChange, type]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Input_styles_Container, {
    width: width,
    children: [title ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(Input_styles_Title, {
      children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_MadatoryAsterisk, {
        children: mandatory && title ? '*' : ''
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(Input
    // required={mandatory}
    , {
      small: small,
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
var usehooks = __webpack_require__(30);

// CONCATENATED MODULE: ./src/components/fabricWidget/index.tsx




function FabricWidget(props) {
  const {
    luName,
    flowName,
    editor,
    error,
    updateValues,
    saveRef
  } = props;
  const ref = Object(react["useRef"])(null);
  const [widgetRefData, setWidgetRefData] = Object(react["useState"])(null);
  const refclickAway = Object(usehooks["a" /* useClickAway */])(() => {
    const editors = widgetRefData === null || widgetRefData === void 0 ? void 0 : widgetRefData.getValues();
    if (editors.length >= 0) {
      updateValues(editors.map(it => ({
        value: it.value,
        name: it.name,
        schema: it.schema
      })));
    }
  });
  Object(react["useEffect"])(() => {
    var _window, _window$k2widgets;
    const onWidgetLoad = data => {
      let editors = !Array.isArray(editor) ? [editor] : editor;
      editors.forEach(editor => {
        if (editor && editor.schema2) {
          data.updateValue(editor.name, editor.value, editor.schema2);
        }
      });
      setWidgetRefData(data);
      saveRef(data);
      // save it in task Data
    };
    const disposeWidget = ref => {
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
  Object(react["useEffect"])(() => {
    if (!widgetRefData) {
      return;
    }
    const values = widgetRefData.getValues();
    const keys = values.map(it => it.name).filter(it => it);
    const editor_names = editor.map(it => it.name);
    const plugins_to_add = [];
    for (let i = 0; i < editor_names.length; i++) {
      const name = editor_names[i];
      if (keys.indexOf(name) < 0) {
        plugins_to_add.push(editor[i]);
      }
    }
    for (let i = 0; i < keys.length; i++) {
      const key = keys[i];
      if (editor_names.indexOf(key) < 0) {
        widgetRefData.removePluginByName(keys[i]);
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

const NumberOfEntities_styles_Container = styled_components_browser_esm["b" /* default */].div`
`;
// CONCATENATED MODULE: ./src/components/NumberOfEntities/index.tsx





function NumberOfEntities(props) {
  var _errors$num_of_entiti;
  const {
    width,
    title,
    placeholder
  } = props;
  const {
    register,
    clearErrors,
    errors,
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    num_of_entities,
    maxToCopy,
    clone_ind,
    selection_method
  } = taskData;
  const onChange = Object(react["useCallback"])(value => {
    saveForm({
      num_of_entities: value
    });
  }, [saveForm]);
  let isRequired = true;
  if (selection_method === 'P' || selection_method == 'PR' || selection_method == 'C') {
    if ((maxToCopy || 0) == 9007199254740992) {
      isRequired = false;
    }
  }
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(NumberOfEntities_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
      ...register('num_of_entities', {
        value: num_of_entities || '',
        validate: value => {
          console.log('value = ', value);
          if ((maxToCopy || 0) === 9007199254740992 && isRequired === false) {
            return true;
          }
          console.log('num_of_entities = ', num_of_entities);
          if (isRequired && !value) {
            return 'Populate Number of Entities';
          }
          if (value < 1) {
            return 'Minimum Entities to Copy is 1';
          }
          if (maxToCopy !== undefined && value > maxToCopy) {
            return `Maximum Entities to Copy is ${maxToCopy}`;
          }
          return true;
        }
      }),
      disabled: clone_ind,
      width: width || '100%',
      name: "num_of_entities",
      mandatory: isRequired,
      min: 0,
      placeholder: placeholder,
      type: InputTypes.number,
      value: clone_ind ? 1 : num_of_entities || '',
      onChange: onChange,
      title: title,
      max: maxToCopy,
      error: (_errors$num_of_entiti = errors.num_of_entities) === null || _errors$num_of_entiti === void 0 ? void 0 : _errors$num_of_entiti.message
    }, `num_of_entities_${isRequired}_${maxToCopy}`)
  });
}
/* harmony default export */ var components_NumberOfEntities = (NumberOfEntities);
// CONCATENATED MODULE: ./src/components/checkbox/styles.ts

const checkbox_styles_Container = styled_components_browser_esm["b" /* default */].label`
    display: flex;
    align-items: center;
    cursor: pointer;
`;
const checkbox_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    text-align: center;
    color: #2e2e2e;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
`;
const CheckboxInput = styled_components_browser_esm["b" /* default */].input`
    margin: ${props => props.title ? '0px 10px 0px 0px !important' : '0px 0px 0px 0px !important'};
    width: 20px;
    height: 20px;
`;
const checkbox_styles_Icon = styled_components_browser_esm["b" /* default */].img`

`;
// CONCATENATED MODULE: ./src/components/checkbox/index.tsx



function Checkbox(props) {
  const {
    title,
    name,
    value,
    onChange,
    disabled
  } = props;
  const onChangeLocal = Object(react["useCallback"])(event => {
    onChange(event.target.checked);
  }, [onChange]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(checkbox_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CheckboxInput, {
      type: "checkbox",
      disabled: disabled,
      name: name,
      checked: value,
      onChange: onChangeLocal,
      title: title
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(checkbox_styles_Title, {
      children: title
    })]
  });
}
/* harmony default export */ var components_checkbox = (Checkbox);
// CONCATENATED MODULE: ./src/components/TooltipPopover/styles.ts

const TooltipContainer = styled_components_browser_esm["b" /* default */].div`
  position: relative;
`;
// EXTERNAL MODULE: ./node_modules/react-tiny-popover/dist/Popover.js
var Popover = __webpack_require__(22);

// CONCATENATED MODULE: ./src/components/TooltipPopover/index.tsx
// Tooltip.tsx





const TooltipPopover = _ref => {
  let {
    children,
    position,
    body,
    align
  } = _ref;
  const [ref, hovering] = Object(usehooks["b" /* useHover */])();
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
/* harmony default export */ var components_TooltipPopover = (TooltipPopover);
// CONCATENATED MODULE: ./src/components/task/DataMovmentSettings/styles.ts

const DataMovmentSettings_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
const TabsContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    gap: 48px;
    padding-bottom: 28px;
`;
const TabItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    display: flex;
    align-items: center;
    gap: 13px;
    cursor: pointer;
`;
const DataMovmentSettings_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const DataMovmentSettings_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    padding: 22px 0px;
`;
const TabTitle = styled_components_browser_esm["b" /* default */].div`
    position: relative;
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
`;
const SelectedTab = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    height: 3px;
    width: 100%;
    border: solid 1px #f4f3ef;
    background-color: #1483f3;
`;
const styles_Body = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
// CONCATENATED MODULE: ./src/images/entity-icon.svg
/* harmony default export */ var entity_icon = ("js/dist/4aa35f4bd77ca777c9c98d64b8169384.svg");
// CONCATENATED MODULE: ./src/images/table-icon.svg
/* harmony default export */ var table_icon = ("js/dist/f46cfe60fb2ec3de6aea40a9b9ea328a.svg");
// CONCATENATED MODULE: ./src/components/Select/styles.ts

const Select_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: ${props => props.width || '100%'};
    min-width: ${props => props.minWidth || ''};
    max-width: ${props => props.maxWidth || ''};
    max-width: 100%;
    position: relative;
    font-size: 16px;
    .select__input {
        height: unset;
    }
`;
const Select_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 7px;
    display: flex;
    align-items:center;
    gap: 13px;
`;
const OptionContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 25px;
`;
const Select_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span`
    color: red;
`;
const Select_styles_Icon = styled_components_browser_esm["b" /* default */].img`
`;
const DescriptionContainer = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    right: 42px;
    bottom: 7px;
`;
const styles_PopoverTemplate = styled_components_browser_esm["b" /* default */].div`
    padding: 10px;
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    width: max-content;
    max-width: 250px;
    max-height: 400px;
    border-radius:3px;
    border: solid 1px #ccc;
    background-color: #fff;
`;
// EXTERNAL MODULE: ./node_modules/react-select/dist/index-641ee5b8.esm.js + 1 modules
var index_641ee5b8_esm = __webpack_require__(16);

// EXTERNAL MODULE: ./node_modules/react-select/dist/react-select.esm.js + 7 modules
var react_select_esm = __webpack_require__(222);

// CONCATENATED MODULE: ./src/components/Select/index.tsx








const customStyles = {
  control: provided => ({
    ...provided,
    minHeight: '30px',
    height: '30px',
    overflow: 'hidden'
  }),
  valueContainer: provided => ({
    ...provided,
    height: '30px',
    padding: '0 6px',
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    overflow: 'auto' // allow scrolling if too many chips
  }),
  multiValue: provided => ({
    ...provided,
    height: '20px',
    backgroundColor: '#e2e2e2',
    borderRadius: '4px',
    display: 'flex',
    alignItems: 'center',
    padding: '2px',
    margin: '2px'
  }),
  multiValueLabel: provided => ({
    ...provided,
    fontSize: '10px',
    padding: '0 4px'
  }),
  multiValueRemove: provided => ({
    ...provided,
    padding: '0 4px',
    fontSize: '10px'
  }),
  input: provided => ({
    ...provided,
    margin: '0px',
    padding: '0px',
    height: 'unset'
  }),
  indicatorsContainer: provided => ({
    ...provided,
    height: '30px'
  })
};
function TDMSelect(props) {
  const {
    title,
    options,
    value,
    mandatory,
    loading,
    onChange,
    placeholder,
    width,
    minWidth,
    maxWidth,
    error,
    isMulti,
    isClearable,
    disabled,
    enableSelectAll,
    titleIcon,
    maxMenuHeight,
    small
  } = props;
  const {
    Option
  } = index_641ee5b8_esm["o" /* c */];
  const [ref, hovering] = Object(usehooks["b" /* useHover */])();
  const getValues = item => {
    let temp = [];
    if (Array.isArray(item)) {
      item.forEach(it => {
        temp = temp.concat(getValues(it));
      });
    } else if (item.options) {
      temp = temp.concat(getValues(item.options));
    } else {
      temp = [item];
    }
    return temp;
  };
  const localOptions = Object(react["useMemo"])(() => {
    if (isMulti && enableSelectAll) {
      const allValues = getValues(options || []);
      if (!value || value.length === 0 || allValues.length !== value.length) {
        return [{
          label: 'All',
          value: 'All'
        }].concat(options);
      }
    }
    return options;
  }, [options, enableSelectAll, isMulti, value]);
  const onLocalChange = Object(react["useCallback"])(item => {
    if (isMulti) {
      if (item.findIndex(it => it.label === 'All') >= 0) {
        let temp = getValues(options);
        onChange(temp);
        return;
      }
    }
    onChange(item);
  }, [onChange, isMulti, options]);
  const ValueOption = props => {
    var _props$data;
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Option, {
      ...props,
      children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(OptionContainer, {
        children: [props.data.label, props !== null && props !== void 0 && (_props$data = props.data) !== null && _props$data !== void 0 && _props$data.description ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_Icon, {
          src: info_icon
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })
    });
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Select_styles_Container, {
    width: width,
    minWidth: minWidth,
    maxWidth: maxWidth,
    children: [title ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(Select_styles_Title, {
      children: [titleIcon ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_Icon, {
        src: titleIcon
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])("span", {
        children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(Select_styles_MadatoryAsterisk, {
          children: mandatory && title ? '*' : ''
        })]
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(react_select_esm["a" /* default */], {
      isDisabled: disabled,
      placeholder: placeholder || '',
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
      maxMenuHeight: maxMenuHeight || undefined,
      styles: small ? customStyles : undefined
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
const React = __webpack_require__(1);
// eslint-disable-next-line @typescript-eslint/no-var-requires,no-undef
const ReactDOM = __webpack_require__(26);
function ReactToAngularJS(Component, directiveName, angularApp, bindings) {
  bindings = bindings || {};
  if (typeof window === "undefined" || typeof angularApp === "undefined") return;
  angularApp.directive(directiveName, function () {
    return {
      scope: bindings,
      replace: true,
      link: function (scope, element) {
        // Add $scope
        scope.$scope = scope;
        // First render - needed?
        ReactDOM.render(React.createElement(Component, scope), element[0]);

        // Watch for any changes in bindings, then rerender
        const keys = [];
        for (let bindingKey of Object.keys(bindings)) {
          if (bindings[bindingKey] !== "&") {
            keys.push(bindingKey);
          }
        }
        //debugger;
        scope.$watchGroup(keys, () => {
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
const exampleAPIs = {
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
      "environment_id": -1,
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
  "businessentity/1/environment/-1/logicalunits": {
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
      "BILLING.INVOICE_BALANCE": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.INVOICE_BALANCE",
        "PARAM_TYPE": "INTEGER",
        "DESCRIPTION": "Remaining balance on the customer's invoice.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "1",
        "MAX_VALUE": "1000",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.NO_OF_OPEN_INVOICES": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.NO_OF_OPEN_INVOICES",
        "PARAM_TYPE": "INTEGER",
        "DESCRIPTION": "Number of invoices that are currently open.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["0", "1", "2", "3", "4", "5", "6", "7"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.OFFER_DESCRIPTION": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.OFFER_DESCRIPTION",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "Description of the promotional or service offer.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["5G LTE", "10G LTE", "Unlimited call", "450 min", "5G 3G", "100 text", "Roaming special", "5G tether", "10G 3G", "Unlimited text"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.OFFER_START_DATE": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.OFFER_START_DATE",
        "PARAM_TYPE": "DATETIME",
        "DESCRIPTION": "Start date of the current offer.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.SUBSCRIBER_TYPE": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.SUBSCRIBER_TYPE",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "Type or category of the subscriber (e.g. personal business).",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["1", "2", "3", "4"],
        "MIN_VALUE": "1",
        "MAX_VALUE": "4",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.TOTAL_BALANCE_AMOUNT": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.TOTAL_BALANCE_AMOUNT",
        "PARAM_TYPE": "INTEGER",
        "DESCRIPTION": "Total outstanding balance for the subscriber.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "24",
        "MAX_VALUE": "6559",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "BILLING.VIP_STATUS": {
        "BE_ID": "1",
        "LU_NAME": "Billing",
        "PARAM_NAME": "BILLING.VIP_STATUS",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "VIP classification status of the subscriber.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Bronze", "Silver", "Gold", "Platinum"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "billing_params"
      },
      "CUSTOMER.ACTIVTY_DATE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.ACTIVTY_DATE",
        "PARAM_TYPE": "DATETIME",
        "DESCRIPTION": "Date of the most recent customer activity or interaction.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CASE_DATE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CASE_DATE",
        "PARAM_TYPE": "DATETIME",
        "DESCRIPTION": "Date when the case was created or logged.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CASE_STATUS": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CASE_STATUS",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "Current status of the customer case (e.g. open closed in progress).",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Unresolved", "Closed", "Open"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CASE_TYPE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CASE_TYPE",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "Type/category of the customer case (e.g. billing technical).",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["Billing Issue", "Device Issue", "Network Issue"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CITY": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CITY",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "City of the customer's address.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CONTRACT_DESCRIPTION": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CONTRACT_DESCRIPTION",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "Description or title of the customer's contract.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["5G LTE", "Unlimited call", "10G LTE", "450 min", "5G 3G", "100 text", "Roaming special", "5G tether", "10G 3G", "Unlimited text"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.CONTRACT_START_DATE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.CONTRACT_START_DATE",
        "PARAM_TYPE": "DATETIME",
        "DESCRIPTION": "Start date of the customer contract.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.NOTE_DATE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.NOTE_DATE",
        "PARAM_TYPE": "DATETIME",
        "DESCRIPTION": "Date when a note was added to the customer case.",
        "COMBO_INDICATOR": "false",
        "VALID_VALUES": ["\\N"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.NO_OF_OPEN_CASES": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.NO_OF_OPEN_CASES",
        "PARAM_TYPE": "INTEGER",
        "DESCRIPTION": "Number of active or unresolved customer support cases.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["0", "1", "2", "3", "4", "5", "6", "7", "8"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.NO_OF_SUBSCRIBERS": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.NO_OF_SUBSCRIBERS",
        "PARAM_TYPE": "INTEGER",
        "DESCRIPTION": "Number of active subscriber contracts associated with the customer.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["0", "1", "2", "3", "4", "5"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
      },
      "CUSTOMER.STATE": {
        "BE_ID": "1",
        "LU_NAME": "Customer",
        "PARAM_NAME": "CUSTOMER.STATE",
        "PARAM_TYPE": "TEXT",
        "DESCRIPTION": "State or region of the customer's address.",
        "COMBO_INDICATOR": "true",
        "VALID_VALUES": ["HI", "DE", "PR", "TX", "MA", "MD", "IA", "ME", "ID", "MI", "UT", "MN", "MO", "IL", "AE", "IN", "MS", "MT", "AK", "VA", "AL", "AP", "AR", "NC", "ND", "NE", "RI", "AZ", "NH", "NJ", "VT", "NM", "FL", "NV", "WA", "NY", "SC", "SD", "WI", "OH", "GA", "OK", "CA", "WV", "WY", "OR", "KS", "CO", "KY", "PA", "CT", "LA", "TN", "DC"],
        "MIN_VALUE": "\\N",
        "MAX_VALUE": "\\N",
        "LU_PARAMS_TABLE_NAME": "customer_params"
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
  },
  "taskgroup": {
    "result": {
      "allTaskGroups": [{
        "task_group_id": 6,
        "task_group_name": "sivangr3",
        "task_group_desc": "sivan group3 favorite",
        "created_by": "sivan",
        "isPermittedUser": true,
        "favorite": true,
        "has_task_created_by_the_user": false
      }, {
        "task_group_id": 4,
        "task_group_name": "tali1",
        "task_group_desc": "tali1desc1",
        "created_by": "tali",
        "isPermittedUser": true,
        "favorite": false,
        "has_task_created_by_the_user": false
      }, {
        "task_group_id": 5,
        "task_group_name": "sivangr1",
        "task_group_desc": "sivan group1",
        "created_by": "sivan",
        "isPermittedUser": true,
        "favorite": false,
        "has_task_created_by_the_user": false
      }, {
        "task_group_id": 3,
        "task_group_name": "zivtest1",
        "task_group_desc": "zivtest desc1",
        "created_by": "ziv.genat@k2view.com",
        "isPermittedUser": true,
        "favorite": false,
        "has_task_created_by_the_user": false
      }, {
        "task_group_id": 2,
        "task_group_name": "zivtest",
        "task_group_desc": "zivtest desc",
        "created_by": "ziv.genat@k2view.com",
        "isPermittedUser": true,
        "favorite": false,
        "has_task_created_by_the_user": false
      }, {
        "task_group_id": 1,
        "task_group_name": "General",
        "task_group_desc": "General",
        "created_by": "system",
        "isPermittedUser": false,
        "favorite": false,
        "has_task_created_by_the_user": false
      }],
      "myTaskGroups": [],
      "favoritesTaskGroups": []
    },
    "errorCode": "SUCCESS",
    "message": null
  },
  "getTasksPerTaskGroup": {
    "result": [{
      "task_id": 1,
      "task_title": "dsf hjsdf hdlf dashflsd hlfh sdlafh lsdh glahg  lhkljsdf lsdhf lsd lf sadhlf fjlkhd sfhlasdhfl ",
      "isPermittedUser": true,
      "favorite": false,
      "display_task_type": "EXTRACT"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "search": {
    "result": {
      "General": [{
        "display_task_type": "Extract",
        "task_title": "dsf",
        "task_id": 1,
        "isPermittedUser": true,
        "favorite": true
      }]
    },
    "errorCode": "SUCCESS"
  },
  "environmentsbyuser": {
    "result": [{
      "role_last_updated_by": "admin",
      "allowed_delete_before_load": false,
      "environment_id": -2,
      "allowed_number_of_entities_to_read": 1000,
      "environment_created_by": "admin",
      "allowed_replace_sequences": false,
      "environment_last_updated_by": "admin",
      "allowed_random_entity_selection": false,
      "role_created_by": "admin",
      "allow_read": true,
      "environment_type": "BOTH",
      "role_description": "Role for AI Environment",
      "environment_description": "This is the AI Generationa and Training environment.",
      "user_type": "ID",
      "allowed_task_scheduling": false,
      "allowed_request_of_fresh_data": false,
      "role_id": -2,
      "allowed_number_of_reserved_entities": 1000,
      "allowed_entity_versioning": false,
      "role_last_updated_date": "2025-04-17 08:08:33.083747",
      "allowed_number_of_entities_to_copy": 1000,
      "environment_name": "AI",
      "allow_write": true,
      "role_expiration_date": null,
      "environment_point_of_contact_phone1": null,
      "environment_last_updated_date": "2025-04-17 08:08:33.083747",
      "environment_status": "Active",
      "allowed_creation_of_synthetic_data": false,
      "allowed_refresh_reference_data": false,
      "role_creation_date": "2025-04-17 08:08:33.083747",
      "mask_sensitive_data": false,
      "sync_mode": "OFF",
      "role_name": "AI",
      "environment_point_of_contact_first_name": null,
      "user_id": "-1",
      "environment_point_of_contact_last_name": null,
      "environment_point_of_contact_email": null,
      "assignment_type": "all",
      "role_status": "Active",
      "allowed_test_conn_failure": false,
      "environment_creation_date": "2025-04-17 08:08:33.083747",
      "environment_expiration_date": null,
      "environment_point_of_contact_phone2": null,
      "username": "ALL"
    }, {
      "role_last_updated_by": "admin",
      "allowed_delete_before_load": false,
      "environment_id": -1,
      "allowed_number_of_entities_to_read": 1000,
      "environment_created_by": "admin",
      "allowed_replace_sequences": false,
      "environment_last_updated_by": "admin",
      "allowed_random_entity_selection": false,
      "role_created_by": "admin",
      "allow_read": true,
      "environment_type": "SOURCE",
      "role_description": "Role for Synethetic Environment",
      "environment_description": "This is the synthetic environment.",
      "user_type": "ID",
      "allowed_task_scheduling": false,
      "allowed_request_of_fresh_data": false,
      "role_id": -1,
      "allowed_number_of_reserved_entities": 0,
      "allowed_entity_versioning": false,
      "role_last_updated_date": "2025-04-17 08:08:33.083747",
      "allowed_number_of_entities_to_copy": 0,
      "environment_name": "Synthetic",
      "allow_write": false,
      "role_expiration_date": null,
      "environment_point_of_contact_phone1": null,
      "environment_last_updated_date": "2025-04-17 08:08:33.083747",
      "environment_status": "Active",
      "allowed_creation_of_synthetic_data": false,
      "allowed_refresh_reference_data": false,
      "role_creation_date": "2025-04-17 08:08:33.083747",
      "mask_sensitive_data": false,
      "sync_mode": "FORCE",
      "role_name": "Synthetic",
      "environment_point_of_contact_first_name": null,
      "user_id": "-1",
      "environment_point_of_contact_last_name": null,
      "environment_point_of_contact_email": null,
      "assignment_type": "all",
      "role_status": "Active",
      "allowed_test_conn_failure": false,
      "environment_creation_date": "2025-04-17 08:08:33.083747",
      "environment_expiration_date": null,
      "environment_point_of_contact_phone2": null,
      "username": "ALL"
    }, {
      "allow_write": true,
      "environment_point_of_contact_phone1": null,
      "environment_id": 2,
      "environment_created_by": "admin",
      "environment_last_updated_date": "2025-04-17 08:08:34.522128",
      "environment_last_updated_by": "admin",
      "environment_status": "Active",
      "allow_read": true,
      "environment_type": "SOURCE",
      "mask_sensitive_data": false,
      "sync_mode": "OFF",
      "environment_description": "This is the Target environment.",
      "environment_point_of_contact_first_name": null,
      "role_id": 0,
      "environment_point_of_contact_last_name": null,
      "environment_point_of_contact_email": null,
      "assignment_type": "user",
      "environment_creation_date": "2025-04-17 08:08:34.522128",
      "environment_expiration_date": null,
      "environment_point_of_contact_phone2": null,
      "environment_name": "UAT"
    }, {
      "allow_write": false,
      "environment_point_of_contact_phone1": null,
      "environment_id": 1,
      "environment_created_by": "admin",
      "environment_last_updated_date": "2025-04-17 08:08:34.522128",
      "environment_last_updated_by": "admin",
      "environment_status": "Active",
      "allow_read": true,
      "environment_type": "SOURCE",
      "mask_sensitive_data": true,
      "sync_mode": "OFF",
      "environment_description": "This is the Source environment.",
      "environment_point_of_contact_first_name": null,
      "role_id": 0,
      "environment_point_of_contact_last_name": null,
      "environment_point_of_contact_email": null,
      "assignment_type": "user",
      "environment_creation_date": "2025-04-17 08:08:34.522128",
      "environment_expiration_date": null,
      "environment_point_of_contact_phone2": null,
      "environment_name": "Production"
    }],
    "errorCode": "SUCCESS",
    "message": null
  },
  "tasks": {
    "result": [{
      "task_last_updated_date": "2025-04-17 11:48:29.56",
      "task_description": "dsf",
      "filterout_reserved": "NA",
      "be_id": 1,
      "reserve_retention_period_type": "Days",
      "environment_id": 1,
      "selection_method": "L",
      "refresh_reference_data": false,
      "tester": null,
      "be_last_updated_date": "2025-04-17 08:08:34.522128",
      "owners": [],
      "refcount": 0,
      "num_of_entities": 2,
      "selected_subset_task_exe_id": 0,
      "tester_type": null,
      "reserve_note": null,
      "load_entity": false,
      "selected_version_task_exe_id": 0,
      "task_created_by": "ziv.genat@k2view.com",
      "be_last_updated_by": "admin",
      "scheduling_end_date": null,
      "retention_period_type": "Do Not Delete",
      "environment_point_of_contact_phone1": null,
      "processnames": null,
      "testers": [],
      "selection_param_value": "1,2",
      "environment_status": "Active",
      "be_status": "Active",
      "task_last_updated_by": "ziv.genat@k2view.com",
      "selected_ref_version_task_exe_id": 0,
      "task_execution_status": "Active",
      "mask_sensitive_data": true,
      "sync_mode": "ON",
      "enable_execution": true,
      "execution_mode": "INHERITED",
      "replace_sequences": false,
      "environment_point_of_contact_last_name": null,
      "environment_point_of_contact_email": null,
      "be_description": "This is a Business Entity created for the TDM GUI for AI/ML Demo.",
      "reserve_retention_period_value": "5",
      "parameters": null,
      "environment_expiration_date": null,
      "environment_point_of_contact_phone2": null,
      "environment_created_by": "admin",
      "clone_ind": false,
      "roles": [],
      "environment_last_updated_by": "admin",
      "be_creation_date": "2025-04-17 08:08:34.522128",
      "task_id": 1,
      "be_created_by": "admin",
      "custom_logic_lu_name": null,
      "source_environment_id": 1,
      "role_id_orig": 0,
      "scheduler": "immediate",
      "environment_description": "This is the Source environment.",
      "source_env_name": "Production",
      "reserve_ind": false,
      "task_title": "dsf",
      "environment_name": "Production",
      "delete_before_load": false,
      "allow_write": false,
      "owner": null,
      "task_status": "Active",
      "retention_period_value": "-1",
      "executioncount": 0,
      "environment_last_updated_date": "2025-04-17 08:08:34.522128",
      "be_name": "Customer",
      "version_ind": true,
      "task_creation_date": "2025-04-17 11:48:29.56",
      "task_globals": false,
      "environment_point_of_contact_first_name": null,
      "task_type": "EXTRACT",
      "environment_creation_date": "2025-04-17 08:08:34.522128",
      "owner_type": null,
      "creatorRoles": ["k2view_k2v_user"]
    }],
    "errorCode": "SUCCESS",
    "message": null
  }
};
// CONCATENATED MODULE: ./src/apis/task.ts


const runningRequests = {};
const task_toastr = getService('toastr');
const task_fetchData = async (path, body, method) => {
  const response = await window.k2api.invokeFabricWebService(path, body, method);
  if (response.isError) {
    throw new Error(response.message);
  }
  if (response.errorCode === 'FAILED') {
    task_toastr === null || task_toastr === void 0 ? void 0 : task_toastr.error(response.message);
    throw new Error(response.message);
  } else if (response.errorCode === 'WARNING') {
    task_toastr === null || task_toastr === void 0 ? void 0 : task_toastr.warning(response.message);
  }
  return response.result;
};
const invokeFabricWebService = async (path, body, method, force) => {
  if (false) {}
  if (window.k2api && window.k2api.invokeFabricWebService) {
    if (force) {
      return task_fetchData(path, body, method);
    }
    if (runningRequests[path]) {
      clearTimeout(runningRequests[path]);
    }
    return new Promise((resolve, reject) => {
      runningRequests[path] = setTimeout(async () => {
        try {
          const data = await task_fetchData(path, body, method);
          resolve(data);
        } catch (error) {
          reject(error);
        }
      }, 100);
    });
  }
  throw new Error('window.k2api is not defined');
};
const getActiveBEs = async () => invokeFabricWebService('getActiveBusinessentities', {}, 'GET');
const getEnvironments = async (be_name, force) => {
  if (be_name) {
    return invokeFabricWebService('userEnvironments', {
      be_name
    }, 'GET', force);
  }
  return invokeFabricWebService('userEnvironments', {}, 'GET', force);
};
const task_getDataGenerationParams = async (taskId, luList) => {
  const body = {};
  if (taskId) {
    body.taskId = taskId;
  }
  if (luList) {
    body.luList = luList;
  }
  return invokeFabricWebService('getDMPopParams', body, 'GET');
};
const getTrainingModels = async (fromDate, toDate, be_name, lu_name) => invokeFabricWebService('tasks/getTrainingModels', {
  fromDate,
  toDate,
  be_name,
  lu_name
}, 'POST');
const getCustomLogicFlows = async (beName, envName) => invokeFabricWebService('getcustomlogicflows', {
  beName,
  envName
}, 'GET');
const getCustomLogicParams = async (luName, flowName) => invokeFabricWebService('getCustomLogicParams', {
  flowName,
  luName
}, 'GET');
const getPreExecutionProcess = async beId => invokeFabricWebService(`businessentity/${beId}/preexecutionprocess`, {}, 'GET');
const getPostExecutionProcess = async beId => invokeFabricWebService(`businessentity/${beId}/postexecutionprocess`, {}, 'GET');
const getTaskPostExecutionProcess = async taskId => invokeFabricWebService(`task/${taskId}/postexecutionprocess`, {}, 'GET');
const getTaskPreExecutionProcess = async taskId => invokeFabricWebService(`task/${taskId}/preexecutionprocess`, {}, 'GET');
const getTaskTables = async taskId => invokeFabricWebService(`task/refsTable/${taskId}`, {}, 'GET');
const getTaskVariables = async taskId => invokeFabricWebService(`task/${taskId}/globals`, {}, 'GET');
const getRetentionPeriodsData = async () => invokeFabricWebService('retentionperiodinfo', {}, 'GET');
const getReferenceTables = async () => invokeFabricWebService('task/getReferenceTaskTable', {}, 'POST');
const task_getEnvironmentOwners = async environmentID => invokeFabricWebService(`environment/${environmentID}/owners`, {}, 'GET');
const getEnvironmentByID = async environmentID => invokeFabricWebService(`environment/${environmentID}`, {}, 'GET');
const getEnvironmentUserRole = async environmentID => invokeFabricWebService(`environment/${environmentID}/userRole`, {}, 'GET');
const getFabricRolesByUser = async user => invokeFabricWebService(`wsGetFabricRolesByUser`, {
  user
}, 'GET');
const checkAIInstallation = async taskType => invokeFabricWebService(`tasks/checkAIInstallation`, {
  taskType
}, 'POST');

// const getTableByEnv = async(source_env: string) => invokeFabricWebService('getTableByEnv', {source_env}, 'POST');
const getTableByBeAndEnv = async (source_env, be_name) => invokeFabricWebService('getTableByBeAndEnv', {
  source_env,
  be_name
}, 'POST');
const getTableVersions = async (table_name, env_name) => invokeFabricWebService('getTableVersions', {
  table_name,
  env_name
}, 'POST');
const getEnvironmentsByUser = async () => invokeFabricWebService('environmentsbyuser', {}, 'GET');
const getGlobalVariables = async lus => invokeFabricWebService('environment/getAllGlobals', {
  lus
}, 'GET');
const validateReservedEntitiesList = async (beID, envID, listOfEntities, filterout_reserved) => invokeFabricWebService('validateReservedEntitiesList', {
  beID,
  envID,
  listOfEntities,
  filterout_reserved
}, 'POST');
const task_getLogicalUnits = async (be_id, env_id) => {
  if (env_id) {
    return invokeFabricWebService(`businessentity/${be_id}/environment/${env_id}/logicalunits`, {}, 'GET');
  }
  return invokeFabricWebService(`businessentity/${be_id}/logicalunits`, {}, 'GET');
};
const getTaskLogicalUnits = async task_id => invokeFabricWebService(`task/${task_id}/logicalunits`, {}, 'GET');
const getParameters = async (be_id, envName) => invokeFabricWebService(`businessentity/${be_id}/sourceEnv/${envName}/parameters`, {}, 'GET');
const getEnableParamWidth = async () => invokeFabricWebService(`wsGetParamsAutoWidth`, {}, 'GET');
const getParamsLUName = async () => invokeFabricWebService(`wsGetParamsLUName`, {}, 'GET');
const getTableParameters = async (dbInterfaceName, SchemaName, tableName) => invokeFabricWebService(`getTableFields`, {
  tableName,
  SchemaName,
  dbInterfaceName
}, 'POST');
const getEntitiesCount = async (be_id, envName, body) => invokeFabricWebService(`businessentity/${be_id}/sourceEnv/${envName}/analysiscount`, body, 'POST');
const task_deleteTask = async (task_id, task_title) => invokeFabricWebService(`task/${task_id}/taskname/${task_title}`, {}, 'DELETE');
const getExecutionProcessParams = async (processType, processesList) => invokeFabricWebService(`getExecutionProcessParams`, {
  processType,
  processesList
}, 'POST');
const getCheckIfParamsCoupling = async () => invokeFabricWebService(`wsCheckIfParamsCoupling`, {}, 'GET');
const getTaskLuEditForTesters = async () => invokeFabricWebService(`wsGetTaskLuEditForTesters`, {}, 'GET');
const getTaskGroups = async () => invokeFabricWebService(`taskgroup`, {}, 'GET');
const getTaskGroupById = async task_group_id => invokeFabricWebService(`getTasksPerTaskGroup`, {
  task_group_id
}, 'GET');
const task_addTaskGroup = async (task_group_name, task_group_desc) => invokeFabricWebService(`taskgroup`, {
  task_group_name,
  task_group_desc
}, 'POST');
const updateTaskGroup = async (task_group_id, task_group_name, task_group_desc) => invokeFabricWebService(`taskgroup/${task_group_id}`, {
  task_group_name,
  task_group_desc
}, 'PUT');
const moveTasksToTaskGroup = async (taskIds, fromTaskGroup, toTaskGroup, keepCurrentGroup) => invokeFabricWebService(`moveTasks`, {
  taskIds,
  fromTaskGroup,
  toTaskGroup,
  keepCurrentGroup
}, 'PUT');
const deleteTaskGroup = async task_group_id => invokeFabricWebService(`taskgroup`, {
  task_group_id
}, 'DELETE');
const searchTasks = async data => invokeFabricWebService(`search`, data, 'POST');
const task_deleteTaskFromGroup = async (task_id, task_group_id) => invokeFabricWebService(`deleteTaskFromTaskGroup`, {
  task_group_id,
  task_id
}, 'DELETE');
const task_toggleTaskGroupFavorite = async (task_group_id, marked) => {
  if (!marked) {
    return invokeFabricWebService(`markFavorite`, {
      task_group_id
    }, 'POST');
  }
  return invokeFabricWebService(`unMarkFavorite`, {
    task_group_id
  }, 'DELETE');
};
const task_toggleTaskFavorite = async (task_id, marked) => {
  if (!marked) {
    return invokeFabricWebService(`markTaskFavorite`, {
      task_id
    }, 'POST');
  }
  return invokeFabricWebService(`unMarkTaskFavorite`, {
    task_id
  }, 'DELETE');
};
const getTaskById = async (task_ids, mode) => invokeFabricWebService(`tasks`, {
  task_ids,
  mode
}, 'GET');
const saveTaskAPI = async taskData => {
  if (taskData.task_id) {
    return invokeFabricWebService(`task/${taskData.task_id}`, taskData, 'PUT');
  }
  return invokeFabricWebService('task', taskData, 'POST');
};
const getVersionsForLoad = async (fromDate, toDate, entitiesList, lu_list, source_env_name, target_env_name, be_id, filterout_reserved) => invokeFabricWebService('tasks/versionsForLoad', {
  fromDate,
  toDate,
  entitiesList,
  lu_list,
  source_env_name,
  target_env_name,
  be_id,
  filterout_reserved
}, 'POST');
const getGenerationExecutions = async (fromDate, toDate, envName, beID, selectedLogicalUnits) => invokeFabricWebService('tasks/getGenerationModels', {
  fromDate,
  toDate,
  envName,
  beID,
  selectedLogicalUnits
}, 'POST');
const taskAPIs = {
  getActiveBEs,
  getEnvironments,
  getDataGenerationParams: task_getDataGenerationParams,
  getTrainingModels,
  getCustomLogicFlows,
  getCustomLogicParams,
  getPreExecutionProcess,
  getPostExecutionProcess,
  getTaskVariables,
  getRetentionPeriodsData,
  getVersionsForLoad,
  getReferenceTables,
  getLogicalUnits: task_getLogicalUnits,
  getGenerationExecutions,
  getParameters,
  getEnableParamWidth,
  getEntitiesCount,
  saveTaskAPI,
  deleteTask: task_deleteTask,
  getTaskLogicalUnits,
  getTableByBeAndEnv,
  getTableVersions,
  getGlobalVariables,
  getTaskPostExecutionProcess,
  getTaskPreExecutionProcess,
  getTableParameters,
  getTaskTables,
  getEnvironmentOwners: task_getEnvironmentOwners,
  getFabricRolesByUser,
  getEnvironmentUserRole,
  getEnvironmentByID,
  checkAIInstallation,
  validateReservedEntitiesList,
  getExecutionProcessParams,
  getParamsLUName,
  getCheckIfParamsCoupling,
  getTaskLuEditForTesters,
  getTaskGroups,
  getTaskGroupById,
  addTaskGroup: task_addTaskGroup,
  toggleTaskGroupFavorite: task_toggleTaskGroupFavorite,
  getTaskById,
  toggleTaskFavorite: task_toggleTaskFavorite,
  moveTasksToTaskGroup,
  deleteTaskGroup,
  updateTaskGroup,
  searchTasks,
  getEnvironmentsByUser,
  deleteTaskFromGroup: task_deleteTaskFromGroup
};
/* harmony default export */ var apis_task = (taskAPIs);
// CONCATENATED MODULE: ./src/components/task/AdvancedBE/styles.ts

const AdvancedBE_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 15px;
    position: relative;
`;
const LogicalUnitsContainer = styled_components_browser_esm["b" /* default */].div`
    width: 400px;
    position: relative;
    z-index: 100;
    max-height: 350px;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 19px 0px 30px 0px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    background-color: #fff;
`;
const AdvancedBE_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.43;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const LogicalUnitTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 18px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    position: relative;
    margin: 0px 20px;
    margin-bottom: 19px;
`;
const LogicalUnitBody = styled_components_browser_esm["b" /* default */].div`
    margin: 10px 25px 0px 30px;
`;
const styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border: solid 1px #ccc;
`;
const CloseIcon = styled_components_browser_esm["b" /* default */].img`
    position: absolute;
    right: 0px;
    top: 5px;
    cursor: pointer;
`;
const AdvancedBE_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const SystemHeader = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    padding: 10px 0px;
    border-bottom: 1px solid #ccc;
    justify-content: space-between;
    padding-right: 11px;
`;
const SystemsContainer = styled_components_browser_esm["b" /* default */].div`

`;
const SystemBody = styled_components_browser_esm["b" /* default */].div`
    padding: 13px 10px 15px 37px;
    background-color: #f2f2f2;
    display: flex;
    gap: 12px;
    flex-direction: column;
    border-bottom: solid 1px #ccc;
`;
const LogicalUnitContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    
`;
const ExecutionModeContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    border-bottom: 1px solid #ccc;
    gap: 10px;
    margin-top: 10px;
    padding-bottom: 10px;
`;
// CONCATENATED MODULE: ./src/images/xclose.svg
/* harmony default export */ var xclose = ("js/dist/f7db0cd4cd00f1f6bb2346432c313b0b.svg");
// CONCATENATED MODULE: ./src/images/arrow-up.svg
/* harmony default export */ var arrow_up = ("js/dist/97a5e48592ebbcad3ed51ba0739a7eba.svg");
// CONCATENATED MODULE: ./src/images/arrow-down.svg
/* harmony default export */ var arrow_down = ("js/dist/e98a6f4185c19c2d8e0ebcb5b207d97e.svg");
// CONCATENATED MODULE: ./src/components/Tabs/styles.ts

const Tabs_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    position: relative;
    font-size: 16px;
`;
const styles_TabTitle = styled_components_browser_esm["b" /* default */].div`
    position: relative;
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: ${props => props.changed ? '#1483f3' : '#2e2e2e'};
`;
const styles_SelectedTab = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    height: 2px;
    width: 100%;
    background-color: #1483f3;
`;
const styles_TabItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    display: flex;
    align-items: center;
    gap: 13px;
    cursor: pointer;
`;
const Tabs_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    margin-left: 5px;
    height: 15px;
    margin-bottom: 3px;
`;
const Tabs_styles_Body = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
const Header = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    gap: 48px;
    padding-bottom: 28px;
`;
// CONCATENATED MODULE: ./src/components/Tabs/index.tsx



function Tabs(props) {
  const {
    tabs,
    selected,
    setSelectedTab,
    children,
    changedTabs
  } = props;
  const getTab = Object(react["useCallback"])(tabData => {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TabItem, {
      onClick: () => setSelectedTab(tabData.name),
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
      children: tabs.map(it => getTab(it))
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tabs_styles_Body, {
      children: children
    })]
  });
}
/* harmony default export */ var components_Tabs = (Tabs);
// CONCATENATED MODULE: ./src/components/task/AdvancedBE/index.tsx













function AdvancedBE() {
  const {
    taskData,
    saveForm,
    register,
    errors,
    unregister,
    allLogicalUnits
  } = Object(react["useContext"])(TaskContext);
  const [data, setData] = Object(react["useState"])([]);
  const [openedSystems, setOpenedSystems] = Object(react["useState"])([]);
  const {
    selected_logical_units,
    execution_mode,
    dataSourceType,
    source_type,
    enable_advanced_for_testers
  } = taskData;
  const [open, setOpen] = Object(react["useState"])(false);
  const [error, setError] = Object(react["useState"])('');
  const [localExecutionMode, setLocalExecutionMode] = Object(react["useState"])('');
  const ref = Object(react["useRef"])();
  const [selectedTab, setSelectedTab] = Object(react["useState"])('Systems & Logical units');
  const AuthService = getService('AuthService');
  const role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
  Object(react["useEffect"])(() => {
    if (execution_mode) {
      setLocalExecutionMode(execution_mode);
    }
  }, [execution_mode]);
  Object(react["useEffect"])(() => {
    if (open) {
      setError('');
      setSelectedTab('Systems & Logical units');
      setLocalExecutionMode(execution_mode || '');
    }
  }, [open]);
  Object(react["useEffect"])(() => {
    if (!allLogicalUnits || allLogicalUnits.length === 0 || !open) {
      return;
    }
    const newData = [];
    allLogicalUnits.forEach(logicalunitItem => {
      const systemFound = newData.find(systemItem => {
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
    newData.forEach(it => {
      const selectedLus = it.logicalUnits.filter(it => it.selected);
      if (selectedLus.length > 0) {
        it.selected = true;
      }
    });
    setData(newData);
  }, [allLogicalUnits, selected_logical_units, open]);
  const systemClick = Object(react["useCallback"])(system => {
    setOpenedSystems(prevSystems => {
      if (prevSystems.indexOf(system) >= 0) {
        return prevSystems.filter(it => it !== system);
      } else {
        return [...prevSystems, system];
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

  const logicalUnitToggle = Object(react["useCallback"])((lu_id, system) => {
    setData(prevData => {
      const newData = JSON.parse(JSON.stringify(prevData));
      const found = newData.find(it => it.system === system);
      if (found) {
        const luFound = found.logicalUnits.find(it => it.lu_id === lu_id);
        if (luFound) {
          luFound.selected = !luFound.selected;
        }
        const selectedLus = found.logicalUnits.filter(it => it.selected);
        if (selectedLus.length > 0) {
          found.selected = true;
        } else {
          found.selected = false;
        }
      }
      return newData;
    });
  }, [setData]);
  const systemToggle = Object(react["useCallback"])(system => {
    setData(prevData => {
      const newData = JSON.parse(JSON.stringify(prevData));
      const found = newData.find(it => it.system === system);
      if (found) {
        found.selected = !found.selected;
        found.logicalUnits.forEach(it => {
          it.selected = found.selected;
        });
      }
      return newData;
    });
  }, [setData]);
  const allAction = Object(react["useCallback"])(flag => {
    setData(prevData => {
      const newData = JSON.parse(JSON.stringify(prevData));
      newData.forEach(it => {
        it.selected = flag;
        it.logicalUnits.forEach(it => {
          it.selected = flag;
        });
      });
      return newData;
    });
  }, [setData]);
  const isSelectedLU = (lu_name, selectedLus) => {
    return selectedLus.findIndex(it => it.lu_name === lu_name) >= 0;
  };
  const checkGap = (lu, selectedLus) => {
    if (lu.lu_parent_name && !isSelectedLU(lu.lu_parent_name, selectedLus)) {
      /**
       * If a logical unit has a parent and it has not being selected
       * then this is the only chance that we might have a gap.
       * The gap will occur if the lu parent name that is missing
       * has a parent which is not missing. This will generate a gap.
       */

      const luParent = allLogicalUnits.find(it => it.lu_name === lu.lu_parent_name);
      if (luParent) {
        if (luParent.lu_parent_name && isSelectedLU(luParent.lu_parent_name, selectedLus)) {
          return luParent.lu_name;
        }
      }
    }
    return '';
  };
  let checkIfRootIsMissing = selectedLus => {
    const missingRootLU = [];

    // check if there is lu which has a parent that does not have a parent (root)
    selectedLus.forEach(lu => {
      if (lu.lu_parent_name) {
        const luParent = allLogicalUnits.find(it => it.lu_name === lu.lu_parent_name);
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
  const SaveData = Object(react["useCallback"])(() => {
    const selectedLus = [];
    data.forEach(it => {
      it.logicalUnits.forEach(it => {
        if (it.selected) {
          selectedLus.push(it);
        }
      });
    });
    if (selectedLus.length === 0) {
      setError('Please choose at least one logical unit');
      return;
    }
    let luGap = '';
    selectedLus.forEach(lu => {
      if (luGap) {
        return;
      }
      luGap = checkGap(lu, selectedLus);
    });
    if (luGap) {
      setError(`There is a gap in the LU hierarchy. Add ${luGap} to complete the gap.`);
      return;
    }
    const missingRoot = checkIfRootIsMissing(selectedLus);
    if (missingRoot.length > 0) {
      setError(`Root LU missing, Please add ${missingRoot.join(', ')} .`);
      return;
    }
    saveForm({
      selected_logical_units: selectedLus.map(it => it.lu_id),
      selected_logical_units_names: selectedLus.map(it => it.lu_name),
      execution_mode: localExecutionMode
    });
    setOpen(false);
  }, [data, saveForm, setError, localExecutionMode]);
  const tabs = Object(react["useMemo"])(() => {
    if (`${dataSourceType}_${source_type}` !== 'data_source_BE') {
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
  const getSelectedTab = Object(react["useCallback"])(() => {
    if (selectedTab === 'Systems & Logical units') {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(Actions, {
          border: true,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: () => allAction(false),
            children: "Clear all"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: () => allAction(true),
            children: "Add all"
          })]
        }), data.map(it => /*#__PURE__*/Object(jsx_runtime["jsxs"])(SystemsContainer, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(SystemHeader, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
              name: `system_${it.system}`,
              title: it.system,
              onChange: () => {
                systemToggle(it.system);
              },
              disabled: role && role.type === 'tester' && !enable_advanced_for_testers,
              value: it.selected
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedBE_styles_Icon, {
              onClick: () => systemClick(it.system),
              src: openedSystems.indexOf(it.system) >= 0 ? arrow_up : arrow_down
            })]
          }), openedSystems.indexOf(it.system) >= 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(SystemBody, {
            children: it.logicalUnits.map(luItem => /*#__PURE__*/Object(jsx_runtime["jsx"])(LogicalUnitContainer, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                name: `logical_unit_${luItem.lu_name}}`,
                title: luItem.lu_name,
                onChange: () => {
                  logicalUnitToggle(luItem.lu_id, it.system);
                },
                disabled: role && role.type === 'tester' && !enable_advanced_for_testers,
                value: luItem.selected
              })
            }))
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
        }))]
      });
    } else if (selectedTab === 'Execution Mode') {
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionModeContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Use the task's Business Entity execution mode",
          onChange: () => setLocalExecutionMode('INHERITED'),
          name: "execution_mode",
          value: "INHERITED",
          selectedValue: localExecutionMode,
          title: "Use Business Entity execution mode"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Executes entire hierarchy for each entity ID",
          onChange: () => setLocalExecutionMode('VERTICAL'),
          name: "execution_mode",
          value: "VERTICAL",
          selectedValue: localExecutionMode,
          title: "Vertical execution"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          tooltip: "Executes system by system, processing all entities for each system",
          onChange: () => setLocalExecutionMode('HORIZONTAL'),
          name: "execution_mode",
          value: "HORIZONTAL",
          selectedValue: localExecutionMode,
          title: "Horizontal execution"
        })]
      });
    }
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
  }, [selectedTab, setLocalExecutionMode, localExecutionMode, allAction, data, systemClick, openedSystems, logicalUnitToggle, role, enable_advanced_for_testers]);
  const getLogicalUnitTemplate = () => {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(LogicalUnitsContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(LogicalUnitTitle, {
        children: ["Advanced BE", /*#__PURE__*/Object(jsx_runtime["jsx"])(CloseIcon, {
          onClick: () => setOpen(false),
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
            onClick: () => setOpen(false),
            children: "Cancel"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionItem, {
            onClick: () => SaveData(),
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
        onClick: () => setOpen(!open),
        children: "Advanced"
      })
    })
  });
}
/* harmony default export */ var task_AdvancedBE = (AdvancedBE);
// CONCATENATED MODULE: ./src/components/task/DataMovmentSettings/index.tsx









const tabTypes = ['be', 'tables'];
const tabNames = {
  be: 'Business Entity',
  tables: 'Tables'
};
const tabIcons = {
  be: entity_icon,
  tables: table_icon
};
function DataMovmentSettings(props) {
  var _errors$be_name;
  const {
    type,
    enabledTabs
  } = props;
  const {
    taskData,
    saveForm,
    register,
    errors,
    statusesFuncMap
  } = Object(react["useContext"])(TaskContext);
  const {
    tableList,
    be_id,
    be_type
  } = taskData;
  const [beData, setBeData] = Object(react["useState"])([]);
  const [selectedTab, setSelectedTab] = Object(react["useState"])();
  const [loading, setLoading] = Object(react["useState"])(false);
  const [selectedBe, setSelectedBe] = Object(react["useState"])();
  const [tabs, setTabs] = Object(react["useState"])([]);
  const [confirmOpen, setConfirmOpen] = Object(react["useState"])(null);
  Object(react["useEffect"])(() => {
    async function fetchData() {
      try {
        const data = await apis_task.getActiveBEs();
        data.forEach(item => {
          item.value = item.be_id;
          item.label = item.be_name;
        });
        setBeData(data);
        setLoading(false);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }
    fetchData();
  }, []);
  Object(react["useEffect"])(() => {
    if (be_id && beData && beData.length > 0) {
      const found = beData.find(it => it.be_id === be_id);
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
  const beChangeLocal = Object(react["useCallback"])((item, choose_option) => {
    if (item.be_id === be_id) {
      return;
    }
    setSelectedBe(item);
    const updateData = {
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
  Object(react["useEffect"])(() => {
    if (tabs && tabs.length > 0) {
      setSelectedTab(tabs[0]);
    }
  }, [tabs]);
  const getTab = Object(react["useCallback"])(tabName => {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TabItem, {
      onClick: () => setSelectedTab(tabName),
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataMovmentSettings_styles_Icon, {
        src: tabIcons[tabName]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TabTitle, {
        children: [tabNames[tabName], selectedTab === tabName ? /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectedTab, {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      })]
    });
  }, [setSelectedTab, selectedTab]);
  Object(react["useEffect"])(() => {
    const filteredTabs = tabTypes.filter(it => {
      if (!enabledTabs || enabledTabs.length === 0) {
        return true;
      }
      return enabledTabs.indexOf(it) >= 0;
    });
    setTabs(filteredTabs);
  }, [enabledTabs]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataMovmentSettings_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        ...register('be_name', {
          required: 'Please Choose Business Entity'
        }),
        width: "290px",
        title: 'Select business entity',
        mandatory: true,
        value: selectedBe,
        options: beData,
        loading: loading,
        onChange: beChangeLocal,
        error: (_errors$be_name = errors.be_name) === null || _errors$be_name === void 0 ? void 0 : _errors$be_name.message
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(task_AdvancedBE, {})]
    })
  });
}
/* harmony default export */ var task_DataMovmentSettings = (DataMovmentSettings);
// CONCATENATED MODULE: ./src/components/DataGenerationParameters/index.tsx














function DataGenerationParameters(props) {
  const {
    dataGenerationParams,
    chosenParams,
    updateParams,
    updateValues
  } = props;
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    synthetic_type,
    be_id,
    sourceUserRole
  } = taskData;
  const [paramsRefData, setParamsRefData] = Object(react["useState"])(null);
  const [paramSelectionName, setParamSelectionName] = Object(react["useState"])('');
  Object(react["useEffect"])(() => {
    if (!paramsRefData) {
      return;
    }
    console.log(paramsRefData.getValues());
  }, [paramsRefData]);
  const getInfoIcon = data => {
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
  const addItem = Object(react["useCallback"])(key => {
    updateParams({
      key,
      action: 'add'
    });
  }, [updateParams]);
  const removeItem = Object(react["useCallback"])(key => {
    updateParams({
      key,
      action: 'remove'
    });
  }, [updateParams]);
  const getSelectedIcon = Object(react["useCallback"])((data, key) => {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
      name: `checkbox_generation_${key}`,
      title: key,
      disabled: data.mandatory && true || false,
      onChange: value => {
        if (value) {
          addItem(key);
        } else {
          removeItem(key);
        }
      },
      value: chosenParams.indexOf(key) >= 0
    });
  }, [chosenParams, removeItem, addItem]);
  const getParamItem = Object(react["useCallback"])(key => {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ParamsItem, {
      chosen: chosenParams.indexOf(key) >= 0,
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ParamsItemText, {
        title: key,
        children: getSelectedIcon(dataGenerationParams[key], key)
      }), getInfoIcon(dataGenerationParams[key])]
    });
  }, [dataGenerationParams, chosenParams, getSelectedIcon]);
  const getEditorData = Object(react["useCallback"])(() => {
    if (!dataGenerationParams) {
      return;
    }
    const editor = [];
    chosenParams.forEach(key => {
      editor.push(dataGenerationParams[key].editor);
    });
    return editor;
  }, [chosenParams, dataGenerationParams]);
  const editorData = getEditorData();
  const getParamSelectionOptions = Object(react["useCallback"])(() => {
    if (!dataGenerationParams) {
      return [];
    }
    const keys = Object.keys(dataGenerationParams).sort((item1, item2) => {
      return ('' + item1).localeCompare(item2);
    });
    if (!paramSelectionName) {
      return keys;
    }
    return keys.filter(key => key.indexOf(paramSelectionName) >= 0);
  }, [dataGenerationParams, paramSelectionName]);
  const widgetAPIExist = window && window.k2widgets ? true : false;
  const syntheticTypeChange = Object(react["useCallback"])(syntheticType => {
    saveForm({
      synthetic_type: syntheticType
    });
  }, [saveForm]);
  const updateFabricRefInData = Object(react["useCallback"])(ref => {
    saveForm({
      widgetRefData: ref
    });
  }, [saveForm]);
  Object(react["useEffect"])(() => {
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
          onChange: setParamSelectionName || (() => {}),
          type: InputTypes.text,
          placeholder: "Search..."
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ParamsList, {
          children: getParamSelectionOptions().map(key => getParamItem(key))
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

const SelectTrainingModels_styles_Container = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  width: 100%;
`;
const SelectTrainingModels_styles_Title = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
`;
const DatesContainer = styled_components_browser_esm["b" /* default */].div`
  margin-top: 20px;
  margin-bottom: 38px;
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
  display: flex;
  align-items: center;
  gap: 30px;
`;
const DateItem = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
const LeftSide = styled_components_browser_esm["b" /* default */].div`
  border-right:  ${props => props.hideBorders ? '' : '1px solid #ccc'};
  display: flex;
  flex-direction: column;
  gap: 25px;
  min-width: 350px;
`;
const styles_RightSide = styled_components_browser_esm["b" /* default */].div`
  margin-left: 30px;
  height: 100% ;
  width: calc(100% - 350px);
`;
const SelectTrainingModels_styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div`
    border-bottom:  ${props => props.hideBorders ? '' : '1px solid #ccc'};
    padding-bottom: 10px;
`;
const SelectTrainingModels_styles_SyntheticEntitiesOptions = styled_components_browser_esm["b" /* default */].div`
    align-self: flex-start;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const LUError = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    margin-top: 20px;
    margin-bottom: 20px;
    color: #ed5565;
`;
const DataGeneratorHeader = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: flex-start;
  gap: 0px;
`;
const DataGeneratorEvaluateContainer = styled_components_browser_esm["b" /* default */].div`
  margin-top: 20px;
`;
// EXTERNAL MODULE: ./node_modules/moment/moment.js
var moment = __webpack_require__(4);
var moment_default = /*#__PURE__*/__webpack_require__.n(moment);

// EXTERNAL MODULE: ./node_modules/@tanstack/table-core/build/lib/index.esm.js
var lib_index_esm = __webpack_require__(50);

// CONCATENATED MODULE: ./src/components/SelectTrainingModels/hooks/useTable.tsx







const useTable = saveLocalData => {
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    selected_subset_task_exe_id,
    trainingStartDate,
    trainingEndDate,
    be_name,
    selected_logical_units_names
  } = taskData;
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  const [loading, setLoading] = Object(react["useState"])(true);
  const [data, setData] = Object(react["useState"])([]);
  Object(react["useEffect"])(() => {
    async function fetchData() {
      try {
        if (!trainingStartDate || !trainingEndDate || !be_name || (selected_logical_units_names || []).length !== 1) {
          return;
        }
        // debugger;
        const data = await apis_task.getTrainingModels(trainingStartDate, trainingEndDate, be_name, (selected_logical_units_names || [])[0]);
        data.forEach(item => {
          item.start_execution_time = moment_default()(item.start_execution_time).format('DD MMM YYYY, HH:mm');
        });
        setData(data);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }
    fetchData();
  }, [trainingStartDate, trainingEndDate, be_name, selected_logical_units_names]);
  const setTrainingModel = Object(react["useCallback"])(data => {
    saveLocalData({
      selected_subset_task_exe_id: data.task_execution_id
    });
  }, [saveLocalData]);
  const columns = Object(react["useMemo"])(() => [{
    id: 'select',
    header: '',
    cell: _ref => {
      let {
        row
      } = _ref;
      return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
        className: "px-1",
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: () => setTrainingModel(row.original),
          name: "select_training_model",
          value: row.original.task_execution_id,
          selectedValue: selected_subset_task_exe_id,
          title: ''
        })
      });
    }
  }, columnHelper.accessor(row => row.task_title, {
    id: 'task_title',
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    }),
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Task name"
    })
  }), columnHelper.accessor('task_execution_id', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Task execution id"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('start_execution_time', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Execution time"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('num_of_entities', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Number of entities"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('task_executed_by', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Executed by"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('execution_note', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Execution note"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  })], [columnHelper, selected_subset_task_exe_id, setTrainingModel]);
  return {
    columns,
    data,
    loading
  };
};
/* harmony default export */ var hooks_useTable = (useTable);
// CONCATENATED MODULE: ./src/components/Table/Filter.tsx


function Filter(_ref) {
  let {
    column,
    table
  } = _ref;
  let filterValue = column.getFilterValue() || '';
  const meta = column.columnDef.meta;
  // const firstValue = table
  //   .getPreFilteredRowModel()
  //   .flatRows[0]?.getValue(column.id)
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
    title: "",
    type: (meta === null || meta === void 0 ? void 0 : meta.type) === 'string' || (meta === null || meta === void 0 ? void 0 : meta.type) === undefined ? InputTypes.text : InputTypes.number,
    value: filterValue,
    onChange: v => {
      if ((meta === null || meta === void 0 ? void 0 : meta.type) === 'number') {
        column.setFilterValue(Number.isFinite(v) ? v : undefined);
      } else {
        column.setFilterValue(v || undefined);
      }
    }
  });
}
/* harmony default export */ var Table_Filter = (Filter);
// CONCATENATED MODULE: ./src/components/Table/styles.ts

const Table_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    height: 100%;
    padding: 0;
    overflow-x: auto;
`;
const TableContainer = styled_components_browser_esm["b" /* default */].table`
    width: 100%;
    border-spacing: 0.5px;
    background-color: #ccc;
`;
const Thead = styled_components_browser_esm["b" /* default */].thead`
    height: 100px;
`;
const Tbody = styled_components_browser_esm["b" /* default */].tbody`
`;
const TableRow = styled_components_browser_esm["b" /* default */].tr`
    height: 50px;
`;
const TableHeaderText = styled_components_browser_esm["b" /* default */].div`
    border: 0.5px solid #ccc;
    height: 50px;
    background-color: #1483f3;
    font-family: Roboto;
    font-size: 16px;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #fff;
    padding: 0px 10px;
    display: flex;
    align-items: center;
`;
const TableHeaderFilter = styled_components_browser_esm["b" /* default */].div`
    border: 0.5px solid #ccc;
    height: 50px;
    background-color: #deebf9;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0px 9px;
`;
const TableHeadItem = styled_components_browser_esm["b" /* default */].th`
    padding: 0px;
`;
const TableRowItem = styled_components_browser_esm["b" /* default */].td`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
    background-color: ${props => props.row % 2 === 0 ? '#fff' : '#f2f2f2'};
    padding-left: 8px;
    border: 0.5px solid #ccc;
`;
// EXTERNAL MODULE: ./node_modules/@tanstack/react-table/build/lib/index.esm.js
var build_lib_index_esm = __webpack_require__(23);

// CONCATENATED MODULE: ./src/components/Table/index.tsx





function IndeterminateCheckbox(_ref) {
  let {
    indeterminate,
    className = '',
    ...rest
  } = _ref;
  const ref = Object(react["useRef"])(null);
  Object(react["useEffect"])(() => {
    if (typeof indeterminate === 'boolean') {
      ref.current.indeterminate = !rest.checked && indeterminate;
    }
  }, [ref, indeterminate]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])("input", {
    type: "checkbox",
    ref: ref,
    className: className + ' cursor-pointer',
    ...rest
  });
}
function Table(props) {
  const {
    data,
    columns,
    isExpandable
  } = props;
  const [columnFilters, setColumnFilters] = Object(react["useState"])([]);
  console.log(columnFilters);
  const table = Object(build_lib_index_esm["b" /* useReactTable */])({
    data,
    columns,
    state: {
      columnFilters // 👈  current filters
    },
    onColumnFiltersChange: setColumnFilters,
    // 👈  updater
    getCoreRowModel: Object(lib_index_esm["c" /* getCoreRowModel */])(),
    getFilteredRowModel: Object(lib_index_esm["e" /* getFilteredRowModel */])(),
    // 👈  enable filtering
    getSubRows: row => row.subRows,
    getExpandedRowModel: Object(lib_index_esm["d" /* getExpandedRowModel */])(),
    enableExpanding: isExpandable,
    defaultColumn: {
      minSize: 0,
      size: Number.MAX_SAFE_INTEGER,
      maxSize: Number.MAX_SAFE_INTEGER
    }
  });
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Table_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Thead, {
        children: table.getHeaderGroups().map(headerGroup => /*#__PURE__*/Object(jsx_runtime["jsx"])(TableRow, {
          children: headerGroup.headers.map(header => {
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
        }, headerGroup.id))
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tbody, {
        children: table.getRowModel().rows.map((row, index) => {
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(TableRow, {
            children: row.getVisibleCells().map(cell => {
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

const RangeDatePicker_styles_Container = styled_components_browser_esm["b" /* default */].div`
 margin-bottom: 38px;
`;
const RangeDatePicker_styles_Title = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
  margin-top: 20px;
  margin-bottom: 20px;
`;
const DateContainer = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
  display: flex;
  align-items: center;
  gap: 30px;
`;
const styles_DateItem = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker.min.js
var react_datepicker_min = __webpack_require__(219);
var react_datepicker_min_default = /*#__PURE__*/__webpack_require__.n(react_datepicker_min);

// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker.css
var react_datepicker = __webpack_require__(267);

// EXTERNAL MODULE: ./node_modules/react-datepicker/dist/react-datepicker-cssmodules.css
var react_datepicker_cssmodules = __webpack_require__(269);

// CONCATENATED MODULE: ./src/components/DatePicker/styles.ts

const DatePicker_styles_Container = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
`;
const IconContainer = styled_components_browser_esm["b" /* default */].button`
    width: 40px;
    height: 40px;
    border-radius: 3px;
    border: solid 1px #ccc;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #fff;
`;
const DatePicker_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    width: 15px;
`;
const CustomDatePickerInput = styled_components_browser_esm["b" /* default */].button`
    border-radius: 3px;
    border: solid 1px #ccc;
    width: 120px;
    height: 40px;
    font-size: 15px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
    padding-left: 11px;
    background-color: #fff;
    cursor: pointer;

`;
// CONCATENATED MODULE: ./src/images/calandar-icon.svg
/* harmony default export */ var calandar_icon = ("js/dist/ddc82450adc11897f7d6cf5354ce7994.svg");
// CONCATENATED MODULE: ./src/components/DatePicker/index.tsx







function TDMDatePicker(props) {
  const {
    date,
    onChange,
    minDate
  } = props;
  const CustomInputRef = /*#__PURE__*/Object(react["forwardRef"])((_ref, ref) => {
    let {
      value,
      onClick
    } = _ref;
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
  const {
    title,
    startDate,
    endDate,
    startDateChange,
    endDateChange
  } = props;
  const startDateUpdate = startDate => {
    if (startDate && endDate && startDate > endDate) {
      endDateChange(new Date(startDate.getTime() + 2592000000));
    }
    startDateChange(startDate);
  };
  const endDateUpdate = endDate => {
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
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    trainingStartDate,
    trainingEndDate,
    be_name,
    synthetic_type,
    selected_logical_units_names,
    sourceUserRole,
    disableGeneration,
    evaluation_ind
  } = taskData;
  const {
    columns,
    data,
    loading
  } = hooks_useTable(saveForm);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  Object(react["useEffect"])(() => {
    const updateData = {};
    if (!trainingStartDate) {
      updateData.trainingStartDate = new Date(Date.now() - 2592000000);
    }
    if (!trainingEndDate) {
      updateData.trainingEndDate = new Date();
    }
    if (Object.keys(updateData).length > 0) {
      saveForm(updateData);
    }
    async function fetchCheckAIInstaltion() {
      try {
        await apis_task.checkAIInstallation('AIGeneration');
      } catch (err) {
        saveForm({
          disableGeneration: true
        });
      }
    }
    if (!disableGeneration) {
      fetchCheckAIInstaltion();
    }
  }, []);
  const startDateUpdate = Object(react["useCallback"])(startDate => {
    const updateData = {
      trainingStartDate: startDate
    };
    saveForm(updateData);
  }, [saveForm]);
  const endDateUpdate = Object(react["useCallback"])(endDate => {
    saveForm({
      trainingEndDate: endDate
    });
  }, [saveForm]);
  Object(react["useEffect"])(() => {
    if (sourceUserRole && sourceUserRole.userType === 'tester' && !sourceUserRole.allow_read || disableGeneration) {
      saveForm({
        synthetic_type: 'generated_data'
      });
    }
  }, [sourceUserRole, disableGeneration]);
  const syntheticTypeChange = Object(react["useCallback"])(syntheticType => {
    saveForm({
      synthetic_type: syntheticType
    });
  }, [saveForm]);
  const evaluateGeneratedDataChange = Object(react["useCallback"])(value => {
    saveForm({
      evaluation_ind: value
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
        }), be_name && synthetic_type === 'new_data' ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_NumberOfEntities, {
            width: '315px',
            title: "Number of entities"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
            title: 'Evaluate generated data',
            name: "evaluate_generated_data",
            value: evaluation_ind,
            onChange: evaluateGeneratedDataChange
          })]
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
    }), be_name && synthetic_type === 'new_data' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_RightSide, {
      children: (selected_logical_units_names || []).length !== 1 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(LUError, {
        children: "Select only one LU."
      }) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataGeneratorHeader, {
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
            title: 'Select data generator',
            startDate: trainingStartDate,
            startDateChange: startDateUpdate,
            endDate: trainingEndDate,
            endDateChange: endDateUpdate
          })
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

const EnvironmentSelect_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: auto;
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 20px;
    align-items: flex-end;
    position: relative;
`;
// CONCATENATED MODULE: ./src/hooks/useToast.ts


const useToast = () => {
  const toastr = getService('toastr');
  return Object(react["useMemo"])(() => ({
    success: (message, inForm) => toastr === null || toastr === void 0 ? void 0 : toastr.success(message),
    error: (message, inForm) => toastr === null || toastr === void 0 ? void 0 : toastr.error(message),
    warning: (message, inForm) => toastr === null || toastr === void 0 ? void 0 : toastr.warning(message, '', {
      containerId: inForm ? 'react-toast-container' : undefined
    })
  }), []);
};
/* harmony default export */ var hooks_useToast = (useToast);
// CONCATENATED MODULE: ./src/components/EnvironmentSelect/index.tsx







function EnvironmentSelect(props) {
  const {
    be_name,
    environment_id,
    onChange,
    syntheticType,
    isMandatory,
    mode,
    title
  } = props;
  const toast = hooks_useToast();
  const {
    taskData
  } = Object(react["useContext"])(TaskContext);
  const {
    dataSourceType,
    source_type
  } = taskData;
  const [selectedEnviornment, setSelectedEnviornment] = Object(react["useState"])(null);
  const [loading, setLoading] = Object(react["useState"])(true);
  const [environments, setEnvironments] = Object(react["useState"])([]);
  Object(react["useEffect"])(() => {
    async function fetchData() {
      try {
        const data = await apis_task.getEnvironments(be_name);
        data.forEach(item => {
          item.value = item.environment_id;
          item.label = item.environment_name;
        });
        if (mode === 'SOURCE' && dataSourceType === 'data_source' && source_type === 'tables') {
          const filteredData = data.filter(it => it.allowed_refresh_reference_data !== false);
          if (filteredData.length === 0 || filteredData.filter(it => it.synthetic_indicator === 'None').length === 0) {
            toast.warning('You don’t have permissions to run tables');
          }
          setEnvironments(filteredData);
        } else {
          setEnvironments(data);
        }
        setLoading(false);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }
    fetchData();
  }, [be_name]);
  Object(react["useEffect"])(() => {
    if (loading) {
      return;
    }
    if (syntheticType !== 'None') {
      if (selectedEnviornment && selectedEnviornment.synthetic_indicator === syntheticType) {
        return;
      }
      const syntheticEnvironment = environments.find(it => it.synthetic_indicator === syntheticType);
      setSelectedEnviornment(syntheticEnvironment || null);
      onChange(syntheticEnvironment || null);
    } else if (selectedEnviornment && selectedEnviornment.synthetic_indicator !== 'None') {
      onChange(null);
    }
  }, [syntheticType, environments, selectedEnviornment, loading]);
  Object(react["useEffect"])(() => {
    if (environment_id === undefined || environment_id === null) {
      setSelectedEnviornment(null);
      return;
    }
    environments.forEach(item => {
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
      options: environments.filter(it => it.synthetic_indicator === 'None' && (it.environment_type === mode || it.environment_type === 'BOTH')),
      loading: loading,
      onChange: onChange
    })
  });
}
/* harmony default export */ var components_EnvironmentSelect = (EnvironmentSelect);
// CONCATENATED MODULE: ./src/images/pii-icon.svg
/* harmony default export */ var pii_icon = ("js/dist/4bace6bde9f0c66715224a842651a702.svg");
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/styles.ts

const ReferenceTables_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    border-left: 2px solid #ccc;
    padding-left: 25px;
    display: flex;
    flex-direction: column;
    gap: 17px;
`;
const TablesContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    gap: 7px;
    align-items:center;
    height: 269px;
`;
const SourceTablesContainer = styled_components_browser_esm["b" /* default */].div`
    width: 260px;
    background-color: #f2f2f2;
    padding: 10px 20px 30px 20px;
    height: calc(100% - 40px);
    height: -webkit-fill-available;
`;
const SourceTables = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 0px;
    overflow-y: auto;
    height: calc(100% - 40px);
`;
const MoveTablesButton = styled_components_browser_esm["b" /* default */].div`
    width: 27px;
    height: 27px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    border-radius: 3px;
    background-color: #1483f3;
`;
const SelectedTables = styled_components_browser_esm["b" /* default */].div`
    flex-grow: 1;
    height: 269px;
`;
const ReferenceTables_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: ${props => props.width || ''};
`;
const ReferenceTables_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
`;
const EnvIconContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 10px;
    cursor: pointer;
`;
const TablesIconContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 12px;
    padding-left: 23px;
    margin-top: 7px;
    cursor: pointer;
`;
const Tables = styled_components_browser_esm["b" /* default */].div` 
    margin-top: 7px;
`;
const TableItemContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    background-color: ${props => props.selected ? '#e5e5e5' : ''};
    padding-left: 48px;  
`;
const ToggleAll = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 10px;
`;
const ToggleTitle = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    font-size: 14px;
`;
const TableTitle = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 12px;
`;
const SelectAllContainer = styled_components_browser_esm["b" /* default */].div`
    width: 12px;
    cursor: pointer;
`;
// CONCATENATED MODULE: ./src/images/env-icon.svg
/* harmony default export */ var env_icon = ("js/dist/7f89285037360b34caf7b2d2a1f8b24e.svg");
// CONCATENATED MODULE: ./src/images/tables-folder-icon.svg
/* harmony default export */ var tables_folder_icon = ("js/dist/35e3caef8acbaf5c22616d0e546dfc6e.svg");
// CONCATENATED MODULE: ./src/images/arrow-right.svg
/* harmony default export */ var arrow_right = ("js/dist/873f030ee47d4d2b143fcec667670d37.svg");
// CONCATENATED MODULE: ./src/components/RegularTable/styles.ts

const RegularTable_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    height: 100%;
    padding: 0;
    overflow-x: auto;
    border: 1px solid #ccc;
    border-radius: 6px;
`;
const styles_TableContainer = styled_components_browser_esm["b" /* default */].table`
    width: 100%;
    max-height: 100%;
    border-spacing: 0.5px;
`;
const styles_Thead = styled_components_browser_esm["b" /* default */].thead`
    height: 55px;
    border-bottom: 1px solid #ccc;
`;
const styles_Tbody = styled_components_browser_esm["b" /* default */].tbody`
`;
const styles_TableRow = styled_components_browser_esm["b" /* default */].tr`
    height: 34px;
    background-color: ${props => props.isSelected ? '#e6f2ff !important' : 'transparent !important'};
    border-left: ${props => props.isSelected ? '4px solid #1683f2' : '4px solid transparent'};
    transition: 'background-color 0.2s, border 0.2s';
`;
const styles_TableHeaderText = styled_components_browser_esm["b" /* default */].div`
    height: 50px;
    font-family: Roboto;
    font-size: 16px;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    padding: 0px 10px;
    display: flex;
    align-items: center;
`;
const styles_TableHeadItem = styled_components_browser_esm["b" /* default */].th`
    padding: 0px;
    border: 0;
    border-bottom: 1px solid #ccc;
    background-color: transparent;

`;
const styles_TableRowItem = styled_components_browser_esm["b" /* default */].td`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
    padding-left: 18px;
    border: 0;
    border-bottom: 1px solid #ccc;
`;
// CONCATENATED MODULE: ./src/images/delete-icon-gray.svg
/* harmony default export */ var delete_icon_gray = ("js/dist/5897937ebe94cc6e33a4e9596d08f998.svg");
// CONCATENATED MODULE: ./src/components/RegularTable/index.tsx





function RegularTable(props) {
  const {
    data,
    columns,
    isExpandable,
    onDeleteSelected,
    enableSelection = false,
    rowKey = 'id' // default key name
  } = props;
  const [selectedKeys, setSelectedKeys] = Object(react["useState"])(new Set());
  const [lastSelectedKey, setLastSelectedKey] = Object(react["useState"])(null);
  const table = Object(build_lib_index_esm["b" /* useReactTable */])({
    data,
    columns,
    getCoreRowModel: Object(lib_index_esm["c" /* getCoreRowModel */])(),
    getSubRows: row => row.subRows,
    getExpandedRowModel: Object(lib_index_esm["d" /* getExpandedRowModel */])(),
    defaultColumn: {
      minSize: 0,
      size: Number.MAX_SAFE_INTEGER,
      maxSize: Number.MAX_SAFE_INTEGER
    },
    enableExpanding: isExpandable
  });
  const keyOf = row => row[rowKey];
  const isAllSelected = data.length > 0 && selectedKeys.size === data.length;
  const toggleSelection = (clickedRow, event) => {
    if (!enableSelection) return;
    const clickedKey = keyOf(clickedRow);
    setSelectedKeys(prevSelected => {
      const newSelected = new Set(prevSelected);
      if (event.shiftKey && lastSelectedKey !== null) {
        const lastIndex = data.findIndex(row => keyOf(row) === lastSelectedKey);
        const clickedIndex = data.findIndex(row => keyOf(row) === clickedKey);
        const [start, end] = [lastIndex, clickedIndex].sort((a, b) => a - b);
        for (let i = start; i <= end; i++) {
          newSelected.add(keyOf(data[i]));
        }
      } else if (event.ctrlKey || event.metaKey) {
        newSelected.has(clickedKey) ? newSelected.delete(clickedKey) : newSelected.add(clickedKey);
      } else {
        newSelected.has(clickedKey) ? newSelected.delete(clickedKey) : newSelected.add(clickedKey);
      }
      return newSelected;
    });
    setLastSelectedKey(clickedKey);
  };
  const toggleSelectAll = () => {
    if (!enableSelection) return;
    setSelectedKeys(isAllSelected ? new Set() : new Set(data.map(keyOf)));
  };
  const deleteRows = () => {
    if (!enableSelection) return;
    const rowsToDelete = selectedKeys.size > 0 ? data.filter(row => selectedKeys.has(keyOf(row))) : [...data];
    onDeleteSelected === null || onDeleteSelected === void 0 ? void 0 : onDeleteSelected(rowsToDelete);
    setSelectedKeys(new Set());
  };
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(RegularTable_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TableContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Thead, {
        children: table.getHeaderGroups().map(headerGroup => /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRow, {
          children: headerGroup.headers.map((header, colIdx, arr) => /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableHeadItem, {
            colSpan: header.colSpan,
            children: enableSelection && colIdx === arr.length - 1 ? /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
              style: {
                display: 'flex',
                alignItems: 'flex-start',
                paddingLeft: '18px',
                justifyContent: 'flex-start',
                gap: '8px',
                height: '100%'
              },
              children: data.length > 0 && /*#__PURE__*/Object(jsx_runtime["jsx"])("img", {
                src: delete_icon_gray,
                alt: selectedKeys.size > 0 ? "Delete selected" : "Delete all",
                title: selectedKeys.size > 0 ? "Delete selected" : "Delete all",
                style: {
                  width: 16,
                  height: 16,
                  cursor: 'pointer',
                  display: 'inline-block'
                },
                onClick: deleteRows
              })
            }) : !header.isPlaceholder && /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableHeaderText, {
              children: Object(build_lib_index_esm["a" /* flexRender */])(header.column.columnDef.header, header.getContext())
            })
          }, header.id))
        }, headerGroup.id))
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_Tbody, {
        children: table.getRowModel().rows.map(row => {
          const rowData = row.original;
          const rowKeyValue = keyOf(rowData);
          const isSelected = selectedKeys.has(rowKeyValue);
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRow, {
            onClick: e => toggleSelection(rowData, e),
            isSelected: isSelected,
            style: {
              cursor: enableSelection ? 'pointer' : 'default'
            },
            children: row.getVisibleCells().map(cell => /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TableRowItem, {
              row: row.index,
              children: Object(build_lib_index_esm["a" /* flexRender */])(cell.column.columnDef.cell, cell.getContext())
            }, cell.id))
          }, row.id);
        })
      })]
    })
  });
}
/* harmony default export */ var components_RegularTable = (RegularTable);
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/useTable.tsx





const useTable_useTable = (deleteRow, toggleModalUpdateVersion, showVersion) => {
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();

  // const { } = useContext(TaskContext);

  const columns = Object(react["useMemo"])(() => [columnHelper.accessor('reference_table_name', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Table name"
    }),
    cell: _ref => {
      let {
        row
      } = _ref;
      return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
        title: `Interface: ${row.original.interface_name}, schema: ${row.original.schema_name}.`,
        children: row.original.reference_table_name
      });
    }
  }), ...(showVersion ? [columnHelper.accessor('version_task_name', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Table version"
    }),
    cell: _ref2 => {
      let {
        row
      } = _ref2;
      return /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
        style: {
          cursor: 'pointer',
          color: '#1683f2'
        },
        onClick: () => toggleModalUpdateVersion(row.original),
        children: row.original.version_task_name || 'None'
      });
    }
  })] : []), {
    id: 'actions',
    header: '',
    cell: _ref3 => {
      let {
        row
      } = _ref3;
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
        onClick: $event => {
          $event.stopPropagation();
          deleteRow(row.original);
        },
        src: delete_icon_gray
      });
    }
  }], [columnHelper, deleteRow, showVersion, toggleModalUpdateVersion]);
  return {
    columns
  };
};
/* harmony default export */ var ReferenceTables_useTable = (useTable_useTable);
// CONCATENATED MODULE: ./src/components/CustomerTypeTable/styles.ts

const UpdateTableVersionContainerstyled = styled_components_browser_esm["b" /* default */].div`
  box-sizing: border-box ;
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 100;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: rgba(0, 0, 0, 0.2);
`;
const styles_animation = styled_components_browser_esm["c" /* keyframes */]`
  from {
    transform: scale(0.9);
  }

  to {
    transform: scale(1);
  }
`;
const CustomerTypeTable_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
box-sizing: border-box ;
    background: white;
    box-sizing: border-box;
    border-radius: 4px;
    box-shadow: 0 0 6px 0 rgba(0, 0, 0, 0.16);
    display: flex;
    flex-direction: column;
    position: relative;
    animation: ${styles_animation} 0.2s linear;
    width: 85%;
    background-color: #ffffff;
    flex-direction: column;
    justify-content: space-between;
    align-items: center;
`;
const HeaderWrapper = styled_components_browser_esm["b" /* default */].div`
    box-sizing: border-box;
    background-color: #1483f3;
    width: 100%;
    height: 40px;
    display: flex;
    justify-content: space-between;
    align-self: start;
    align-items: center ;

    color: white;
    gap: 10px;
    font-size: 18px ;
    padding: 0 10px;
`;
const WrapperTop = styled_components_browser_esm["b" /* default */].div`
    box-sizing: border-box;
    width: 100%;
    height: 100px;
    display: flex;
    justify-content: start;
    align-items: center;
    gap: 10px;
    padding: 20px;
`;
const WrapperBottom = styled_components_browser_esm["b" /* default */].div`
    box-sizing: border-box;
    width: 100%;
    height: 300px;
    display: flex;
    justify-content: start;
    align-items: center;
    padding: 20px;
    gap: 10px;
`;
const WrapperFooter = styled_components_browser_esm["b" /* default */].div`
box-sizing: border-box ;
    width: 100%;
    padding: 0 20px;
    display: flex;
    align-items: center ;
    justify-content: flex-end;
`;
const styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const styles_ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
// CONCATENATED MODULE: ./src/components/CustomerTypeTable/index.tsx





const MyIcon = _ref => {
  let {
    color = 'white'
  } = _ref;
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
const CustomerTypeTable = _ref2 => {
  let {
    onClose,
    data,
    column,
    onClickSave,
    tableName
  } = _ref2;
  const onSave = () => {
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
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(MyIcon, {})
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(WrapperTop, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RangeDatePicker, {
          title: '',
          startDate: new Date(),
          startDateChange: () => {},
          endDate: new Date(),
          endDateChange: () => {}
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
            onClick: () => onClose(),
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
/* harmony default export */ var components_CustomerTypeTable = (CustomerTypeTable);
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/useCustomerTypeTable.tsx
// {

//     "task_name": "aaaa",

//     "task_description": "",

//     "executed_by": "[tahata@k2view.com##[k2view_k2v_user]](mailto:tahata@k2view.com )",

//     "execution_datetime": "2024-02-13 07:46:01.232883",

//     "task_execution_id": 1,

//     "number_of_records": 10

//   }







const useCustomerTable = () => {
  const {
    taskData: {
      source_environment_name,
      tableList
    },
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const [data, setData] = Object(react["useState"])([]);
  const [selected, setSelected] = Object(react["useState"])(null);
  const [current, setCurrent] = Object(react["useState"])(null);
  const selectRow = row => {
    setSelected({
      task_execution_id: row.task_execution_id,
      task_name: row.task_name
    });
  };
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  const [showModal, setShowModal] = Object(react["useState"])(false);
  const columns = Object(react["useMemo"])(() => [{
    id: 'select',
    header: '',
    cell: _ref => {
      let {
        row
      } = _ref;
      return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
        className: "px-1",
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: () => selectRow(row.original),
          name: "select",
          value: row.original.task_execution_id,
          selectedValue: selected === null || selected === void 0 ? void 0 : selected.task_execution_id,
          title: ''
        })
      });
    }
  }, columnHelper.accessor('task_name', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "task name"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('task_execution_id', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "task execution id"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('execution_datetime', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Creation date"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('executed_by', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "executed by"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('task_description', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "task description"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  })], [columnHelper, selected]);
  const OpenModalUpdateVersion = Object(react["useCallback"])(async row => {
    //inputs table_name,env_name for api getTableVersions
    if (source_environment_name) {
      const request = {
        table_name: row.reference_table_name,
        env_name: source_environment_name
      };
      const data = await apis_task.getTableVersions(row.reference_table_name, request.env_name);
      setData(data);
      setShowModal(true);
      setCurrent(row);
      setSelected({
        task_execution_id: row.version_task_execution_id,
        task_name: row.version_task_name
      });
    }
  }, [source_environment_name]);
  const onClose = () => {
    setShowModal(false);
  };
  const onClickSave = () => {
    const updatedData = (tableList || []).map(table => table.reference_table_name === current.reference_table_name ? {
      ...table,
      version_task_execution_id: selected === null || selected === void 0 ? void 0 : selected.task_execution_id,
      version_task_name: selected === null || selected === void 0 ? void 0 : selected.task_name
    } : table);
    saveForm({
      tableList: updatedData
    });
  };
  return {
    columns,
    OpenModalUpdateVersion,
    showModal,
    onClose,
    data,
    onClickSave,
    current
  };
};
/* harmony default export */ var useCustomerTypeTable = (useCustomerTable);
// CONCATENATED MODULE: ./src/components/task/ReferenceTables/index.tsx















function ReferenceTables(props) {
  const {
    taskData,
    saveForm,
    register,
    errors
  } = Object(react["useContext"])(TaskContext);
  const {
    source_type,
    //BE or tables
    be_name,
    source_environment_name,
    fetchPolicy,
    tableList,
    sync_mode,
    selected_logical_units_names
  } = taskData;
  const showVersion = Object(react["useMemo"])(() => fetchPolicy === 'load_snapshot' || fetchPolicy === 'available_data', [fetchPolicy]);
  const [tablesData, setTablesData] = Object(react["useState"])([]);
  const [loading, setLoading] = Object(react["useState"])(false);
  const [filter, setFilter] = Object(react["useState"])('');
  Object(react["useEffect"])(() => {
    async function fetchData() {
      try {
        if (!source_environment_name) {
          return;
        }
        let data = await apis_task.getTableByBeAndEnv(source_environment_name || '', be_name);
        const transformedEnvs = data.map((item, index) => {
          // Extract database name and tables

          const dbName = Object.keys(item)[0]; // e.g., 'CRM_DB'

          const schemas = {};
          item[dbName].forEach(schema => {
            const schemaKey = Object.keys(schema)[0];
            schemas[schemaKey] = {
              tables: [],
              opened: false,
              selectAll: false
            };
            const tables = schema[schemaKey].filter(it => (sync_mode === 'OFF' && it.taskExecutionId || sync_mode !== 'OFF') && ((selected_logical_units_names || []).indexOf(it.luName) >= 0 || !be_name)).map(table => {
              const old = tableList != null ? tableList : [];
              const isFound = old.find(el => el.reference_table_name === table.tableName);
              const isMoved = !!isFound;
              return {
                name: table.tableName,
                version_task_execution_id: showVersion ? table.taskExecutionId : null,
                version_task_name: showVersion ? table.taskName : null,
                schema_name: schemaKey,
                lu_name: table.luName,
                selected: false,
                moved: isMoved
              };
            });
            schemas[schemaKey].tables = tables;
            schemas[schemaKey].opened = false;
            schemas[schemaKey].selectAll = false;
          });

          // Construct and return the Env object
          return {
            env_name: dbName,
            opened: false,
            // Set default values
            openedTables: true,
            // Set default values
            schemas: schemas
          };
        });
        const newTableList = tableList === null || tableList === void 0 ? void 0 : tableList.filter(item_selected => {
          const interfaceData = transformedEnvs.find(item => item.env_name === item_selected.interface_name);
          if (interfaceData && interfaceData.schemas[item_selected.schema_name]) {
            const schemaData = interfaceData.schemas[item_selected.schema_name];
            if (schemaData && schemaData.tables.findIndex(it => it.name === item_selected.reference_table_name) >= 0) {
              return true;
            }
          }
          return false;
        });
        saveForm({
          tableList: newTableList
        });
        setTablesData(transformedEnvs);
      } catch (err) {
        // use hook toast
        console.log(err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [be_name, source_environment_name, source_type, sync_mode, selected_logical_units_names, showVersion]);
  Object(react["useEffect"])(() => {
    const old = tableList != null ? tableList : [];
    setTablesData(prevEnvsState => {
      return prevEnvsState.map(env => {
        const updatedSchemas = Object.keys(env.schemas).reduce((acc, schemaKey) => {
          if (!acc[schemaKey]) {
            acc[schemaKey] = {
              tables: [],
              opened: env.schemas[schemaKey].opened,
              selectAll: env.schemas[schemaKey].selectAll
            };
          }
          const updatedTables = env.schemas[schemaKey].tables.map(table => {
            const isFound = old.find(el => el.reference_table_name === table.name && el.schema_name === table.schema_name && el.interface_name === env.env_name);
            const isMoved = !!isFound;
            return {
              ...table,
              moved: isMoved,
              selected: false
            };
          });
          acc[schemaKey].tables = updatedTables;
          return acc;
        }, {});
        console.log('updatedSchemas', updatedSchemas);
        return {
          ...env,
          schemas: updatedSchemas
        };
      });
    });
  }, [source_environment_name, tableList]);
  const toggleBE = Object(react["useCallback"])(BE => {
    setTablesData(prevData => {
      const foundBE = prevData.find(it => it.env_name === BE);
      if (foundBE) {
        foundBE.opened = !foundBE.opened;
      }
      return [...prevData];
    });
  }, [setTablesData]);
  const toggleTables = Object(react["useCallback"])((BE, schemaKey) => {
    setTablesData(prevData => {
      const foundBE = prevData.find(it => it.env_name === BE);
      if (foundBE) {
        foundBE.schemas[schemaKey].opened = !foundBE.schemas[schemaKey].opened;
      }
      return [...prevData];
    });
  }, [setTablesData]);
  const toggleTable = Object(react["useCallback"])((BE, table_name, schemaKey) => {
    setTablesData(prevData => {
      const foundBE = prevData.find(it => it.env_name === BE);
      if (foundBE) {
        const table = foundBE.schemas[schemaKey].tables.find(it => it.name === table_name);
        if (table) {
          table.selected = !table.selected;
        }
      }
      return [...prevData];
    });
  }, [setTablesData]);
  const [lastSelectedMap, setLastSelectedMap] = Object(react["useState"])({});
  const getTable = Object(react["useCallback"])((BE, tableData, schemaKey, _index) => {
    const schemaKeyFull = `${BE}.${schemaKey}`;
    const tableKey = `${BE}.${schemaKey}.${tableData.name}`;
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableItemContainer, {
      selected: tableData.selected,
      onClick: e => {
        setTablesData(prevData => {
          const newData = [...prevData];
          const foundBE = newData.find(it => it.env_name === BE);
          if (!foundBE) return prevData;
          const schema = foundBE.schemas[schemaKey];
          if (!schema) return prevData;
          const tables = schema.tables;
          const clickedIndex = tables.findIndex(t => t.name === tableData.name);
          if (clickedIndex === -1) return prevData;
          const lastKey = lastSelectedMap[schemaKeyFull];
          const lastIndex = lastKey != null ? tables.findIndex(t => `${BE}.${schemaKey}.${t.name}` === lastKey) : -1;
          if (e.shiftKey && lastIndex !== -1) {
            // Select range
            const [start, end] = [lastIndex, clickedIndex].sort((a, b) => a - b);
            for (let i = start; i <= end; i++) {
              tables[i].selected = true;
            }
          } else {
            // Toggle single
            tables[clickedIndex].selected = !tables[clickedIndex].selected;
            setLastSelectedMap(prev => ({
              ...prev,
              [schemaKeyFull]: tableKey
            }));
          }
          return newData;
        });
      },
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SelectAllContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
          name: `table_${BE}_${schemaKey}_${tableData.name}`,
          title: '',
          onChange: () => {},
          value: tableData.selected
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
        width: '17px',
        src: table_icon
      }), tableData.name]
    });
  }, [lastSelectedMap]);
  const getEnvTables = Object(react["useCallback"])((env, schem, schemaKey) => {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: env.opened ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TablesIconContainer, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SelectAllContainer, {
            title: schem.selectAll ? 'Unselect all' : 'Select all',
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
              name: `table_${env.env_name}_${schemaKey}`,
              title: '',
              onChange: value => {
                toggleTableInterfaces(env.env_name, schemaKey, value);
              },
              value: schem.selectAll
            })
          }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TableTitle, {
            onClick: () => toggleTables(env.env_name, schemaKey),
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
              width: '21px',
              src: tables_folder_icon
            }), schemaKey]
          })]
        }), schem.opened ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables, {
          children: schem.tables.filter(it => it.name.toLowerCase().indexOf((filter || '').toLowerCase()) >= 0 || !filter).filter(it => !it.moved).map((it, index) => getTable(env.env_name, it, schemaKey, index))
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
    });
  }, [filter, getTable, toggleBE, toggleTables]);
  const movedTables = Object(react["useMemo"])(() => tableList != null ? tableList : [], [tableList]);
  const moveTables = () => {
    const old = tableList != null ? tableList : [];
    const updatedTables = [];
    tablesData.forEach(env => {
      // Iterate over each schema within the environment
      Object.entries(env.schemas).forEach(_ref => {
        let [schemaKey, schema] = _ref;
        // Filter for selected tables within this schema
        const movedTables = schema.tables.filter(table => table.selected);
        movedTables.forEach(table => {
          updatedTables.push({
            id: `${env.env_name}_${table.schema_name}_${table.name}`,
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
    setTablesData(prevData => {
      prevData.forEach(env_interface => {
        Object.keys(env_interface.schemas).forEach(schemaKey => {
          env_interface.schemas[schemaKey].selectAll = false;
        });
      });
      return [...prevData];
    });
    saveForm({
      tableList: [...old, ...updatedTables]
    });
  };
  const deleteRow = Object(react["useCallback"])(row => {
    let updatedTables = tableList || [];
    updatedTables = updatedTables.filter(table => {
      return !(table.reference_table_name === row.reference_table_name && table.schema_name === row.schema_name && table.interface_name === row.interface_name);
    });
    saveForm({
      tableList: updatedTables
    });
  }, [tableList, saveForm]);
  const {
    columns: customerTableColumns,
    data: customerTableData,
    OpenModalUpdateVersion,
    showModal,
    onClose,
    onClickSave,
    current
  } = useCustomerTypeTable();
  const {
    columns
  } = ReferenceTables_useTable(deleteRow, OpenModalUpdateVersion, showVersion);

  /**
   * Toggle the `selected` flag for *every* table in every schema of every env.
   * Passing `true` checks all the check-boxes, passing `false` clears them.
   */
  const toggleTableInterfaces = Object(react["useCallback"])((BE, schemaKey, value) => {
    setTablesData(prevData => {
      const foundBE = prevData.find(it => it.env_name === BE);
      if (foundBE && foundBE.schemas && foundBE.schemas[schemaKey] && foundBE.schemas[schemaKey].tables) {
        foundBE.schemas[schemaKey].selectAll = value;
        foundBE.schemas[schemaKey].tables.forEach(table => {
          if (!table.moved) {
            table.selected = value || false;
          }
        });
      }
      return [...prevData];
    });
  }, []);
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
          children: tablesData.map(env => /*#__PURE__*/Object(jsx_runtime["jsxs"])(react_default.a.Fragment, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(EnvIconContainer, {
              onClick: () => toggleBE(env.env_name),
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
                width: '19px',
                src: env_icon
              }), env.env_name]
            }), Object.entries(env.schemas).map(_ref2 => {
              let [schemaKey, schem] = _ref2;
              return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
                children: getEnvTables(env, schem, schemaKey)
              }, schemaKey);
            })]
          }, env.env_name))
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(MoveTablesButton, {
        onClick: moveTables,
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(ReferenceTables_styles_Icon, {
          src: arrow_right
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SelectedTables, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_RegularTable, {
          columns: columns,
          data: movedTables,
          enableSelection: true,
          onDeleteSelected: rowsToDelete => {
            const updatedTables = tableList === null || tableList === void 0 ? void 0 : tableList.filter(table => {
              return !rowsToDelete.some(row => row.reference_table_name === table.reference_table_name && row.schema_name === table.schema_name && row.interface_name === table.interface_name);
            });
            saveForm({
              tableList: updatedTables
            });
          }
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













function DataSourceSettingsForm(props) {
  const {
    taskData,
    saveForm,
    unregister
  } = Object(react["useContext"])(TaskContext);
  const {
    be_name,
    dataSourceType,
    source_environment_id,
    mask_sensitive_data,
    sync_mode,
    synthetic_type,
    source_environment_name,
    environment_sync_mode,
    version_ind,
    be_id,
    source_type,
    tables_selected,
    sourceUserRole,
    fetchPolicy,
    generateChosenParams,
    dataGenerationParams,
    enable_masking_only,
    target_env
  } = taskData;
  const [maskSensitiveDataLocal, setMaskSensitiveDataLocal] = Object(react["useState"])(mask_sensitive_data || false);
  Object(react["useEffect"])(() => {
    const updateData = {};
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
  const fetchDataPolicyItems = Object(react["useMemo"])(() => {
    const dataMapper = {
      new_data: {
        value: 'new_data',
        label: `Available data from the Test data store, new data from ${source_environment_name}`
      },
      all_data: {
        value: 'all_data',
        label: `All data from ${source_environment_name}`
      },
      load_snapshot: {
        value: 'load_snapshot',
        label: `Selected snapshot (version)`
      },
      available_data: {
        value: 'available_data',
        label: `Available ${source_environment_name} data in the Test data store`
      }
    };
    let result = [];
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
      result = result.filter(it => it.value !== 'all_data');
    }
    return result;
  }, [environment_sync_mode, source_environment_name, dataSourceType, source_type, sourceUserRole]);
  const [fecthDataPolicyLocal, setFecthDataPolicyLocal] = Object(react["useState"])(fetchDataPolicyItems[0]);
  Object(react["useEffect"])(() => {
    setMaskSensitiveDataLocal(mask_sensitive_data || false);
  }, [mask_sensitive_data]);
  Object(react["useEffect"])(() => {
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

  Object(react["useEffect"])(() => {
    const getDataByValue = value => {
      return fetchDataPolicyItems.find(it => it.value === value);
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
  Object(react["useEffect"])(() => {
    if (dataSourceType === 'data_source' && source_type === 'tables') {
      saveForm({
        tables_selected: true
      });
    }
  }, [dataSourceType, source_type]);
  const dataSourceTypeChange = Object(react["useCallback"])(dataSourceTypeNew => {
    let source_type = '';
    let dataSourceType = dataSourceTypeNew;
    const updateBE = {
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
    if (dataSourceTypeNew === 'ai_generated' && target_env === 'ai_training') {
      updateBE.target_env = undefined;
    }
    saveForm({
      source_environment_id: undefined,
      source_environment_name: '',
      sync_mode: 'ON',
      version_ind: false,
      dataSourceType,
      source_type,
      ...updateBE
    });
  }, [saveForm]);
  const envChangeLocal = Object(react["useCallback"])(item => {
    const sync_mode = item && item.environment_sync_mode || 'ON';
    const syncModeData = {};
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
    saveForm({
      source_environment_id: item && item.environment_id || undefined,
      source_environment_name: item && item.environment_name || undefined,
      synthetic_indicator: item && item.synthetic_indicator || false,
      mask_sensitive_data: item && item.mask_sensitive_data || false,
      environment_sync_mode: item && item.environment_sync_mode || 'ON',
      ...syncModeData
    });
  }, [saveForm, dataSourceType, source_type]);
  Object(react["useEffect"])(() => {
    if (environment_sync_mode === 'OFF') {
      if (['new_data', 'all_data'].indexOf(fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value) >= 0) {
        saveForm({
          sync_mode: 'OFF',
          version_ind: false
        });
      }
    }
  }, [environment_sync_mode]);
  const updateGenerationParamValues = Object(react["useCallback"])(values => {
    if (values && values.length > 0) {
      const copyGenerationParams = JSON.parse(JSON.stringify(dataGenerationParams));
      values.forEach(data => {
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
  const updateChosenParams = Object(react["useCallback"])(data => {
    const updateData = {};
    const copyDataGenerationParams = JSON.parse(JSON.stringify(dataGenerationParams));
    const copyGenerateChosenParams = [...generateChosenParams];
    if (data.action === 'add') {
      if (copyGenerateChosenParams.length === 0) {
        copyDataGenerationParams[data.key].order = 1;
      } else {
        const key = copyGenerateChosenParams[copyGenerateChosenParams.length - 1];
        copyDataGenerationParams[data.key].order = copyDataGenerationParams[key].order + 1;
      }
      updateData.dataGenerationParams = copyDataGenerationParams;
      updateData.generateChosenParams = [...copyGenerateChosenParams, data.key];
    } else {
      copyDataGenerationParams[data.key].editor.value = copyDataGenerationParams[data.key].default;
      copyDataGenerationParams[data.key].value = copyDataGenerationParams[data.key].default;
      copyDataGenerationParams[data.key].order = 99999999;
      updateData.dataGenerationParams = copyDataGenerationParams;
      updateData.generateChosenParams = copyGenerateChosenParams.filter(key => key !== data.key);
    }
    saveForm(updateData);
  }, [dataGenerationParams, saveForm, generateChosenParams]);
  const fetchDataPolicyChange = Object(react["useCallback"])(item => {
    const updateData = {};
    if ((item === null || item === void 0 ? void 0 : item.value) === 'new_data') {
      updateData.sync_mode = 'ON';
      updateData.version_ind = false;
    } else if ((item === null || item === void 0 ? void 0 : item.value) === 'all_data') {
      updateData.sync_mode = 'FORCE';
      updateData.tableList = [];
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
  Object(react["useEffect"])(() => {
    if ((fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value) !== fetchPolicy) {
      saveForm({
        fetchPolicy: fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value
      });
    }
  }, [fecthDataPolicyLocal]);
  const syntheticTypeMapper = {
    data_source: 'None',
    synthetic: 'RuleBased',
    ai_generated: 'AI'
  };
  const getDataPolicy = Object(react["useCallback"])(() => {
    let value = fetchDataPolicyItems.find(it => it.value === (fecthDataPolicyLocal === null || fecthDataPolicyLocal === void 0 ? void 0 : fecthDataPolicyLocal.value));
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
          selectedValue: `${dataSourceType}_${source_type}`,
          title: "Entities & referential data"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
          onChange: dataSourceTypeChange,
          name: "data_source_type",
          value: "data_source_tables",
          selectedValue: `${dataSourceType}_${source_type}`,
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
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataSourceSettings_styles_Icon, {
                src: pii_icon
              }), maskSensitiveDataLocal ? 'Sensitive data is masked' : 'Data is not masked']
            }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
          }), source_environment_id ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(FetchDataPolicyContainer, {
            children: [getDataPolicy(), source_type !== 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                name: 'reference_tables',
                disabled: !(sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_refresh_reference_data),
                title: 'Referential tables',
                onChange: value => {
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

const DataSubset_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 30px;
`;
const DataSubset_styles_Container = styled_components_browser_esm["b" /* default */].div`
    max-width: 80vw;
    position: relative;
`;
const DataSubsetsTypes = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
    padding-bottom: 30px;
    border-bottom: solid 1px #ccc;
    width: 872px;
`;
const styles_EnvironmentsContainer = styled_components_browser_esm["b" /* default */].div`
    width: 290px;
`;
const DataSubset_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const DatasetIconContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 8px;
`;
const SelectMethodSelectContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    gap: 25px;
    align-items: flex-start;
`;
// export const SelectMethodSelectContainer = styled.div`
//     width: 350px;
// `

const DataVersioningContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
`;
const DataGenerationContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
`;
const GenerationTypeOptions = styled_components_browser_esm["b" /* default */].div`
    align-self: flex-start;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const DataSubset_styles_Seprator = styled_components_browser_esm["b" /* default */].span`
    border-right: 1px solid #ccc;
    width: 1px;
    height: ${props => props.expand ? '105px' : '70px'};
`;
const NumberOfEntitiesContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
`;
// CONCATENATED MODULE: ./src/components/TextArea/styles.ts

const TextArea_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
const TextArea_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 10px;
`;
const TextArea_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span`
    color: red;
`;
const TextArea = styled_components_browser_esm["b" /* default */].textarea`
    font-family: Roboto;
    font-size: 15px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
    padding: 9px 10px;
    border-radius: 3px;
    border: solid 1px #ccc;
    width: -webkit-fill-available;
    width: -moz-available;
    resize: none;
    :placeholder{
        font-size: 15px;
        font-weight: normal;
        font-stretch: normal;
        font-style: normal;
        line-height: 1.33;
        letter-spacing: normal;
        text-align: left;
        color: #999;
    }
`;
// CONCATENATED MODULE: ./src/components/TextArea/index.tsx




function TDMTextArea(props) {
  const {
    title,
    value,
    onChange,
    name,
    mandatory,
    placeholder,
    error
  } = props;
  const onChangeLocal = Object(react["useCallback"])(event => {
    onChange(event.target.value);
  }, [onChange]);
  console.log(value);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TextArea_styles_Container, {
    children: [title ? /*#__PURE__*/Object(jsx_runtime["jsxs"])(TextArea_styles_Title, {
      children: [title, /*#__PURE__*/Object(jsx_runtime["jsx"])(TextArea_styles_MadatoryAsterisk, {
        children: mandatory ? '*' : ''
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(TextArea, {
      rows: 6,
      placeholder: placeholder,
      name: name,
      value: value || '',
      onChange: onChangeLocal
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
      error: error
    })]
  });
}
/* harmony default export */ var components_TextArea = (TDMTextArea);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/EntityList.tsx





function EntityList(props) {
  var _errors$selection_par;
  const {
    register,
    clearErrors,
    errors,
    unregister,
    resetField,
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    selection_param_value,
    maxToCopy
  } = taskData;
  const localChange = Object(react["useCallback"])(value => {
    saveForm({
      selection_param_value: value,
      num_of_entities: (value || '').split(',').length
    });
  }, [saveForm]);
  const validateEntites = Object(react["useCallback"])(value => {
    if (value && value.split(',').length > (maxToCopy || 0)) {
      return `The number of entities cannot exceed ${maxToCopy || 0} entities.`;
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
  Object(react["useEffect"])(() => {
    unregister('selection_param_value');
    return () => {
      unregister('selection_param_value');
    };
  }, []);
  console.log(selection_param_value);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Container, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TextArea, {
      ...register('selection_param_value', {
        value: selection_param_value,
        required: 'Populate entities',
        validate: {
          validateEntites: validateEntites
        }
      }),
      name: "selection_param_value",
      title: "Enter entity IDs separated by commas",
      mandatory: true,
      min: 1,
      value: selection_param_value,
      onChange: localChange,
      error: (_errors$selection_par = errors.selection_param_value) === null || _errors$selection_par === void 0 ? void 0 : _errors$selection_par.message
    })
  });
}
/* harmony default export */ var DataSubset_EntityList = (EntityList);
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/CustomLogic/styles.ts

const CustomLogic_styles_Container = styled_components_browser_esm["b" /* default */].div`
    border-top: 1px solid #ccc;
    padding-top: 30px;
    width: 100%;
    display: flex;
    position: relative;
`;
const styles_LeftSide = styled_components_browser_esm["b" /* default */].div`
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 25px;
    width: 326px;
    border-right: 1px solid #ccc;
`;
const CustomLogic_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border-right: 1px solid #ccc;
    width: 1px;
    position: absolute;
    height: calc(100% + 80px);
    top: 0px;
    left: 400px;
`;
const SelectContainer = styled_components_browser_esm["b" /* default */].div`
    width: 287px;
`;
const Params = styled_components_browser_esm["b" /* default */].div`
    width: calc(100% - 400px);
    margin-left: 60px;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/CustomLogic/index.tsx









function CustomLogic(props) {
  const {
    taskData,
    saveForm,
    register
  } = Object(react["useContext"])(TaskContext);
  const {
    selection_param_value,
    parameters,
    be_name,
    source_environment_name,
    environment_name,
    customLogicParams,
    maxToCopy
  } = taskData;
  const [loading, setLoading] = Object(react["useState"])(true);
  const [customLogicFlows, setCustomLogicFlows] = Object(react["useState"])([]);
  const [selectedCustomLogicFlow, setSelectedCustomLogicFlow] = Object(react["useState"])(null);
  Object(react["useEffect"])(() => {
    async function fetchCutomLogicFlows() {
      try {
        if (!be_name || !(environment_name || source_environment_name)) {
          setLoading(false);
          return;
        }
        const data = await apis_task.getCustomLogicFlows(be_name, environment_name || source_environment_name || '');
        data.forEach(flow => {
          flow.value = `${flow.luName}#${flow.flowName}`;
          flow.label = flow.flowName;
          flow.description = flow.Description;
        });
        if (selection_param_value) {
          const found = data.find(it => it.flowName === selection_param_value);
          if (found) {
            setSelectedCustomLogicFlow(found);
          }
        }
        setCustomLogicFlows(data);
        setLoading(false);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }
    fetchCutomLogicFlows();
  }, []);
  Object(react["useEffect"])(() => {
    async function fetchCutomLogicParams() {
      try {
        if (!selectedCustomLogicFlow) {
          return;
        }
        console.log(selectedCustomLogicFlow);
        const data = await apis_task.getCustomLogicParams(selectedCustomLogicFlow.luName, selectedCustomLogicFlow.flowName);
        data.forEach(param => {
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
            const params = JSON.parse(parameters);
            if (params && params.inputs) {
              data.forEach(customParam => {
                if (customParam.type === 'bool' && !customParam.default) {
                  customParam.default = false;
                }
                const param = params.inputs.find(it => it.name === customParam.name);
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
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
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

  const updateFabricEditorValues = values => {
    values.forEach(data => {
      updateCustomParamLogicNative(data.name, data.value);
    });
  };
  const updateCustomParamLogicNative = Object(react["useCallback"])((name, value) => {
    if (!customLogicParams) {
      return;
    }
    const customParamsTemp = [...customLogicParams];
    const index = customParamsTemp.findIndex(param => param.name === name);
    if (index >= 0) {
      customParamsTemp[index].value = value;
      if (customParamsTemp[index] && customParamsTemp[index].editor) {
        const editorTemp = customParamsTemp[index].editor;
        if (editorTemp) {
          editorTemp.value = value;
        }
      }
      const parameters = {
        inputs: (customParamsTemp || []).map(it => {
          return {
            name: it.name,
            type: it.type,
            value: it.value
          };
        })
      };
      saveForm({
        customLogicParams,
        parameters: JSON.stringify(parameters)
      });
    }
  }, [customLogicParams]);
  const updateFabricRefInData = Object(react["useCallback"])(ref => {
    saveForm({
      widgetRefData: ref
    });
  }, [saveForm]);
  const getFabricParams = Object(react["useCallback"])(() => {
    var _ref;
    if (!selectedCustomLogicFlow) {
      return;
    }
    const fabricWidgetItems = customLogicParams === null || customLogicParams === void 0 ? void 0 : customLogicParams.filter(it => it.editor);
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(fabricWidget, {
        editor: (_ref = fabricWidgetItems || []) === null || _ref === void 0 ? void 0 : _ref.map(it => it.editor),
        updateValues: updateFabricEditorValues,
        saveRef: updateFabricRefInData
      })
    });
  }, [customLogicParams, updateFabricEditorValues, updateFabricRefInData, selectedCustomLogicFlow]);
  const getParams = Object(react["useCallback"])(() => {
    if (!selectedCustomLogicFlow) {
      return;
    }
    const regularItems = customLogicParams === null || customLogicParams === void 0 ? void 0 : customLogicParams.filter(it => !it.editor);
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
      children: (regularItems || []).map((param, index) => {
        /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
          ...register(`${param.name}_${index}`, {
            value: param.value,
            required: 'Please type entities'
          }),
          width: "287px",
          title: param.displayName || '',
          mandatory: param.mandatory,
          value: param.value,
          onChange: value => updateCustomParamLogicNative(param.name, value),
          type: param.type === 'integer' || param.type === 'real' ? InputTypes.number : InputTypes.text
        });
      })
    });
  }, [customLogicParams, updateCustomParamLogicNative, selectedCustomLogicFlow]);
  const updateCustomFlow = Object(react["useCallback"])(it => {
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
        title: "Max number of entities"
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
        title: "Select custom logic",
        mandatory: (maxToCopy || 0) < 9007199254740992,
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

const SelectDataVerioning_styles_Container = styled_components_browser_esm["b" /* default */].div`
`;
const SelectDataVerioning_styles_Title = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
`;
const styles_DatesContainer = styled_components_browser_esm["b" /* default */].div`
  margin-top: 20px;
  margin-bottom: 38px;
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
  display: flex;
  align-items: center;
  gap: 30px;
`;
const SelectDataVerioning_styles_DateItem = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
const SelectDataVerioning_styles_Icon = styled_components_browser_esm["b" /* default */].img`
`;
const SelectDataVerioning_styles_TableContainer = styled_components_browser_esm["b" /* default */].div`
    max-width: 80vw;
    overflow: auto;
`;
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/useTable.tsx








const SelectDataVerioning_useTable_useTable = (selected_version_task_exe_id, saveForm) => {
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  const setVersioningData = Object(react["useCallback"])(data => {
    const version_datetime = new Date(data.version_datetime);
    saveForm({
      selected_version_task_name: data.version_name,
      selected_version_succeeded_entities: data.num_of_succeeded_entities,
      selected_version_datetime: moment_default()(version_datetime).format('YYYYMMDDHHmmss'),
      selected_version_task_exe_id: data.task_execution_id
    });
  }, [saveForm]);
  const columnsDef = Object(react["useMemo"])(() => [{
    column: 'version_name',
    name: 'Version Name',
    clickAble: true,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'task_id',
    name: 'Task Id',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'task_execution_id',
    name: 'Task Execution Id',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'version_no',
    name: 'Version Number',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'execution_note',
    name: 'Execution Note',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'task_last_updated_by',
    name: 'Last Updated By',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'version_type',
    name: 'Version Type',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'version_datetime',
    name: 'Creation date',
    type: 'date',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'lu_name',
    name: 'Logical unit Name',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'number_of_extracted_entities',
    name: 'Number of Processed Entities',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'num_of_succeeded_entities',
    name: 'Number of Succeeded Entities',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'num_of_failed_entities',
    name: 'Number of Failed Entities',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }
  // {
  //     column: 'rootIndicator',
  //     name: 'Root LU',
  //     clickAble: false
  // },
  ], []);
  const columns = Object(react["useMemo"])(() => {
    const columnsResult = [];
    columnsResult.push({
      id: 'collapse',
      header: '',
      cell: _ref => {
        let {
          row
        } = _ref;
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
      cell: _ref2 => {
        let {
          row
        } = _ref2;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: () => setVersioningData(row.original),
            name: "select_version_for_load",
            value: '' + row.original.task_execution_id,
            selectedValue: '' + selected_version_task_exe_id,
            title: ''
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsDef.forEach(col => {
      columnsResult.push({
        ...columnHelper.accessor(col.column, {
          header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: col.name
          }),
          cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: info.getValue()
          }),
          meta: col.meta,
          // ✅ pass the meta here
          filterFn: col.filterFn // ✅ pass custom filter if any
        }),
        width: 'auto'
      });
    });
    return columnsResult;
  }, [columnHelper, selected_version_task_exe_id, columnsDef]);
  return {
    columns
  };
};
/* harmony default export */ var SelectDataVerioning_useTable = (SelectDataVerioning_useTable_useTable);
// CONCATENATED MODULE: ./src/components/SelectDataVerioning/groupVersions.ts
const groupData = (data, selectedLus, allLus) => {
  const lusTemp = [];
  (selectedLus || []).forEach(lu => {
    const luTemp = allLus.find(it => it.lu_name === lu);
    if (luTemp) {
      lusTemp.push({
        ...luTemp,
        lu_parent_name: luTemp.lu_parent_name
      });
    }
  });
  const rootLUs = lusTemp.filter(it => !it.lu_parent_name);
  const groupedData = data.reduce((acc, curr) => {
    if (!acc[curr.task_execution_id]) acc[curr.task_execution_id] = []; //If this type wasn't previously stored
    acc[curr.task_execution_id].push(curr);
    return acc;
  }, {});
  const versionsForLoadRoot = [];
  const versionsForLoadSubRoot = [];
  Object.keys(groupedData).forEach(task_execution_id => {
    groupedData[task_execution_id].forEach(versionForLoad => {
      if (rootLUs.findIndex(it => it.lu_name === versionForLoad.lu_name) >= 0 && versionsForLoadRoot.findIndex(it => it.task_execution_id === versionForLoad.task_execution_id) < 0) {
        versionForLoad.rootIndicator = true;
        versionsForLoadRoot.push(versionForLoad);
      } else {
        versionForLoad.rootIndicator = false;
        versionsForLoadSubRoot.push(versionForLoad);
      }
    });
  });
  const versionsForLoad = [...versionsForLoadRoot];
  versionsForLoadSubRoot.forEach(versionForLoad => {
    const foundVersion = versionsForLoad.find(it => it.task_execution_id === versionForLoad.task_execution_id);
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
  const {
    taskData,
    saveForm,
    allLogicalUnits
  } = Object(react["useContext"])(TaskContext);
  const {
    be_id,
    selected_logical_units_names,
    source_environment_name,
    environment_name,
    selection_method,
    selection_param_value,
    versioningStartDate,
    versioningEndDate,
    selected_version_task_exe_id,
    filterout_reserved,
    clone_ind,
    replace_sequences,
    load_entity,
    target_env,
    environment_id,
    sync_mode,
    version_ind
  } = taskData;
  const [data, setData] = Object(react["useState"])([]);
  const [loading, setLoading] = Object(react["useState"])(true);
  const {
    columns
  } = SelectDataVerioning_useTable(selected_version_task_exe_id, saveForm);
  console.log(taskData);
  Object(react["useEffect"])(() => {
    const fetchData = setTimeout(async () => {
      try {
        if (!versioningStartDate || !versioningStartDate || !selected_logical_units_names || !source_environment_name && !environment_name || !be_id) {
          return;
        }
        setLoading(true);
        let local_filterout_reserved = filterout_reserved || 'OTHERS';
        if ((clone_ind || replace_sequences) && !load_entity || target_env === 'ai_training' || !environment_id || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL') {
          local_filterout_reserved = 'NA';
        }
        const data = await apis_task.getVersionsForLoad(versioningStartDate, versioningEndDate, selection_method === 'ALL' ? '' : selection_param_value || '', selected_logical_units_names, source_environment_name, environment_name, be_id, local_filterout_reserved);
        const newData = groupData(data.ListOfVersions, selected_logical_units_names, allLogicalUnits);
        const sortedData = newData.sort((it1, it2) => new Date(it2.version_datetime).getTime() - new Date(it1.version_datetime).getTime());
        if (!sortedData || sortedData.length < 0) {
          saveForm({
            selected_version_task_exe_id: undefined
          });
        }
        setData(sortedData);
        setLoading(false);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }, 500);
    return () => clearTimeout(fetchData);
  }, [selection_param_value, selection_method, versioningStartDate, versioningEndDate, source_environment_name, environment_name, allLogicalUnits, selected_logical_units_names, be_id, filterout_reserved]);
  Object(react["useEffect"])(() => {
    const updateData = {};
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
  Object(react["useEffect"])(() => {
    if (selected_version_task_exe_id && data && data.length > 0) {
      const index = data.findIndex(it => it.task_execution_id === selected_version_task_exe_id);
      if (index < 0) {
        saveForm({
          selected_version_task_exe_id: undefined
        });
      }
    }
  }, [data]);
  const startDateUpdate = Object(react["useCallback"])(startDate => {
    const updateData = {
      versioningStartDate: startDate
    };
    if (startDate && versioningEndDate && startDate > versioningEndDate) {
      updateData.versioningEndDate = new Date(startDate.getTime() + 2592000000);
    }
    saveForm(updateData);
  }, [saveForm, versioningEndDate]);
  const endDateUpdate = Object(react["useCallback"])(endDate => {
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

const SelectGeneratedExecution_styles_Container = styled_components_browser_esm["b" /* default */].div`
`;
const SelectGeneratedExecution_styles_Title = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
`;
const SelectGeneratedExecution_styles_DatesContainer = styled_components_browser_esm["b" /* default */].div`
  margin-top: 20px;
  margin-bottom: 38px;
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  line-height: 1.25;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
  display: flex;
  align-items: center;
  gap: 30px;
`;
const SelectGeneratedExecution_styles_DateItem = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  gap: 10px;
`;
const SelectGeneratedExecution_styles_TableContainer = styled_components_browser_esm["b" /* default */].div`
    max-width: 80vw;
    overflow: auto;
`;
const SelectGeneratedExecution_styles_Icon = styled_components_browser_esm["b" /* default */].img`
`;
// CONCATENATED MODULE: ./src/components/SelectGeneratedExecution/useTable.tsx








const SelectGeneratedExecution_useTable_useTable = () => {
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    selected_subset_task_exe_id
  } = taskData;
  const generationIdChange = Object(react["useCallback"])((selected_subset_task_exe_id, num_of_entities) => {
    saveForm({
      selected_subset_task_exe_id,
      num_of_entities
    });
  }, [saveForm]);
  const columnsDef = Object(react["useMemo"])(() => [{
    column: 'task_title',
    name: 'Generation task name',
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'lu_name',
    name: 'Logical unit Name',
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'task_execution_id',
    name: 'Task execution id',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      console.log(filterValue);
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }, {
    column: 'execution_note',
    name: 'Execution note',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'start_execution_time',
    name: 'Execution Time',
    clickAble: false,
    meta: {
      type: 'string'
    },
    filterFn: (row, id, filterValue) => {
      // cast both sides to strings and do a "contains" check
      return String(row.getValue(id)).toLowerCase().includes(String(filterValue).toLowerCase());
    }
  }, {
    column: 'number_of_entities',
    name: 'Number of generated entities',
    clickAble: false,
    meta: {
      type: 'number'
    },
    filterFn: (row, id, filterValue) => {
      return String(row.getValue(id)).includes(String(filterValue));
    }
  }], []);
  const columns = Object(react["useMemo"])(() => {
    const columnsResult = [];
    columnsResult.push({
      id: 'collapse',
      header: '',
      cell: _ref => {
        let {
          row
        } = _ref;
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
      cell: _ref2 => {
        let {
          row
        } = _ref2;
        return /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
          className: "px-1",
          children: row.depth === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
            onChange: () => generationIdChange(row.original.task_execution_id, row.original.number_of_entities),
            name: "select_generation_execution",
            value: row.original.task_execution_id,
            selectedValue: selected_subset_task_exe_id,
            title: ''
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
        });
      }
    });
    columnsDef.forEach(col => {
      columnsResult.push({
        ...columnHelper.accessor(col.column, {
          header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: col.name
          }),
          cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
            children: info.getValue()
          }),
          meta: col.meta,
          // ✅ pass the meta here
          filterFn: col.filterFn // ✅ pass custom filter if any
        }),
        width: 'auto'
      });
    });
    return columnsResult;
  }, [columnHelper, columnsDef, selected_subset_task_exe_id, generationIdChange]);
  return {
    columns
  };
};
/* harmony default export */ var SelectGeneratedExecution_useTable = (SelectGeneratedExecution_useTable_useTable);
// CONCATENATED MODULE: ./src/components/SelectGeneratedExecution/index.tsx









function SelectGeneratedExecution(props) {
  const {
    dataSourceType
  } = props;
  const {
    taskData,
    saveForm,
    allLogicalUnits
  } = Object(react["useContext"])(TaskContext);
  const {
    generationStartDate,
    generationEndDate,
    source_environment_name,
    be_id,
    selected_logical_units_names,
    onReset
  } = taskData;
  const [data, setData] = Object(react["useState"])([]);
  const [loading, setLoading] = Object(react["useState"])(true);
  const {
    columns
  } = SelectGeneratedExecution_useTable();
  Object(react["useEffect"])(() => {
    console.log(`onReset=${onReset}`);
    if (onReset) {
      const updateData = {};
      updateData.generationStartDate = new Date(Date.now() - 2592000000);
      updateData.generationEndDate = new Date();
      if (Object.keys(updateData).length > 0) {
        saveForm(updateData);
      }
    }
  }, [onReset]);
  Object(react["useEffect"])(() => {
    const fetchData = setTimeout(async () => {
      try {
        if (!generationStartDate || !generationEndDate || !source_environment_name || !be_id || (selected_logical_units_names === null || selected_logical_units_names === void 0 ? void 0 : selected_logical_units_names.length) === 0) {
          return;
        }
        setLoading(true);
        const data = await apis_task.getGenerationExecutions(generationStartDate, generationEndDate, source_environment_name, be_id, selected_logical_units_names);
        const newData = groupData(data, selected_logical_units_names || [], allLogicalUnits);
        const sortedData = newData.sort((it1, it2) => new Date(it2.start_execution_time).getTime() - new Date(it1.start_execution_time).getTime());
        setData(sortedData);
        setLoading(false);
      } catch (err) {
        // use hook toast
        setLoading(false);
      }
    }, 500);
    return () => clearTimeout(fetchData);
  }, [generationStartDate, generationEndDate, source_environment_name, be_id, selected_logical_units_names, allLogicalUnits]);
  Object(react["useEffect"])(() => {
    const updateData = {};
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
  const startDateUpdate = Object(react["useCallback"])(startDate => {
    saveForm({
      generationStartDate: startDate
    });
  }, [saveForm]);
  const endDateUpdate = Object(react["useCallback"])(endDate => {
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

const rotateAnimation = styled_components_browser_esm["c" /* keyframes */]`
100% { -webkit-transform: rotate(360deg); } 
`;
const Parameters_styles_Container = styled_components_browser_esm["b" /* default */].div`
    min-width: 60vw;
    position: relative;
    display: block;
    border-top: 1px solid #ccc;
    padding-top: 15px;
`;
const Parameters_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: 27px;
`;
const AnimationIcon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: 27px;
    animation: ${rotateAnimation} 3s linear infinite
`;
const DateFormatNote = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
`;
const FilterOutReservedContainer = styled_components_browser_esm["b" /* default */].div`
    position: relative;
    display: flex;
    gap: 10px;
    flex-direction: column;
`;
const SQLQuery = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
    margin-top: 30px;
`;
const MaxNumberOfEntitiesContainer = styled_components_browser_esm["b" /* default */].div`
    margin-top: 88px;
`;
const Parameters_styles_LeftSide = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 400px;
    border-right: 1px solid #ccc;
`;
const RefreshParameters = styled_components_browser_esm["b" /* default */].div`
    margin-bottom: 23px;
    display: flex;
    align-items: center;
    gap: 12px;
    font-family: Roboto;
    font-size: 16px;
    font-weight: bolder;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
`;
const Parameters_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border-right: 1px solid #ccc;
    width: 1px;
    position: absolute;
    height: calc(100% + 80px);
    top: 0px;
    left: 400px;
`;
const Parameters_styles_RightSide = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 15px;
`;
// CONCATENATED MODULE: ./src/components/QueryBuilder/styles.ts

const QueryBuilder_styles_Container = styled_components_browser_esm["b" /* default */].div`
    padding: 30px;
    border: 2px solid #cccccc;
    max-width: 100%;
    margin-top: 10px;
    background-color: #f9f9f9;
`;
const RulesContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
const RuleContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    position: relative;
`;
const ActionsContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 10px;
    margin-bottom: 13px;
`;
const RuleItemContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 10px;
    align-items: center;
    height: 45px;
    margin-bottom: 9px;
`;
const MinMaxNote = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
    align-self: center;
    margin-top: 3px;
`;
const RemoveRuleIcon = styled_components_browser_esm["b" /* default */].img`
 margin-left: auto;
`;
const styles_IconContainer = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    left: -22px;
    top: 15px;
`;
const QueryBuilder_styles_Icon = styled_components_browser_esm["b" /* default */].img``;
// CONCATENATED MODULE: ./src/components/Button/styles.ts

const Button_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width:  ${props => props.width || '100%'};
    height: ${props => props.height || '35px'};
    object-fit: contain;
    border-radius: 3px;
    background-color: ${props => {
  if (props.backgroundColor) {
    return props.backgroundColor;
  }
  if (props.type === 'secondary') {
    return '#fff';
  }
  return 'var(--primary-color)';
}};
    display: flex;
    gap: 9px;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    border: ${props => {
  if (props.danger) {
    return 'solid 1px #ff6666';
  }
  if (props.type === 'secondary') {
    return 'solid 1px #1483f3';
  }
  return '0';
}};
`;
const Button_styles_Title = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 11px;
    font-family: Roboto;
    font-size: 15px;
    font-weight: bolder;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: center;
    color: ${props => {
  if (props.danger) {
    return '#ff6666';
  }
  if (props.type === 'secondary') {
    return 'var(--primary-color)';
  }
  return '#fefefe';
}}; 
`;
const Button_styles_Icon = styled_components_browser_esm["b" /* default */].img`
`;
// CONCATENATED MODULE: ./src/components/Button/index.tsx


function Button(props) {
  const {
    title,
    onClick,
    width,
    height,
    type,
    disabled,
    icon,
    danger,
    backgroundColor,
    children
  } = props;
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

const useConstants = () => {
  const operators = Object(react["useMemo"])(() => [{
    label: 'AND',
    name: 'AND',
    value: 'AND'
  }, {
    label: 'OR',
    name: 'OR',
    value: 'OR'
  }], []);
  const conditions = Object(react["useMemo"])(() => [{
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
  }], []);
  const comboConditions = Object(react["useMemo"])(() => [{
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
  }], []);
  const dateConditions = Object(react["useMemo"])(() => [{
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
  }], []);
  return {
    operators,
    conditions,
    comboConditions,
    dateConditions
  };
};
/* harmony default export */ var QueryBuilder_useConstants = (useConstants);
// CONCATENATED MODULE: ./src/components/QueryBuilder/Rule.tsx










function Rule(props) {
  const {
    rule,
    parameters,
    ruleIndex,
    groupIndex,
    lastRule,
    onChange,
    parentGroup,
    type
  } = props;
  const {
    register,
    clearErrors,
    errors,
    config_params
  } = Object(react["useContext"])(TaskContext);
  const [chosenParam, setChosenParam] = Object(react["useState"])();
  const [chosenCondition, setChosenCondition] = Object(react["useState"])();
  const [chosenOperator, setChosenOperator] = Object(react["useState"])();
  const [currentConditions, setCurrentConditions] = Object(react["useState"])([]);
  const {
    operators,
    conditions,
    comboConditions,
    dateConditions
  } = QueryBuilder_useConstants();
  Object(react["useEffect"])(() => {
    if (!rule.field) {
      setChosenParam(undefined);
      setCurrentConditions([]);
      return;
    }
    const currentParam = parameters === null || parameters === void 0 ? void 0 : parameters.find(it => it.param_name === rule.field);
    setChosenParam(currentParam);
    if (currentParam) {
      let condToUse = [];
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
        condToUse = condToUse.map(it => {
          const newItem = Object.assign({}, it);
          newItem.value = newItem.tableValue;
          return newItem;
        });
      }
      setCurrentConditions(condToUse);
    }
  }, [comboConditions, conditions, dateConditions, parameters, rule.field, type]);
  Object(react["useEffect"])(() => {
    setChosenCondition(currentConditions.find(it => it.value === (rule === null || rule === void 0 ? void 0 : rule.condition)));
  }, [rule.condition, currentConditions]);
  Object(react["useEffect"])(() => {
    setChosenOperator(operators.find(it => it.value === (rule === null || rule === void 0 ? void 0 : rule.operator)));
  }, [rule.operator]);
  const onParamChange = Object(react["useCallback"])(item => {
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
  const onConditionChange = Object(react["useCallback"])(item => {
    // setChosenCondition(item);
    rule.condition = item.value;
    rule.data = null;
    onChange();
    //TODO save up
  }, [onChange, rule]);
  const onOperatorChange = Object(react["useCallback"])(item => {
    rule.operator = item.value;
    setChosenOperator(item);
    onChange();
    //TODO save up
  }, [rule, onChange]);
  const onComboValueChange = Object(react["useCallback"])(value => {
    if (Array.isArray(value)) {
      rule.data = value.map(it => it.value);
    } else {
      rule.data = value.value;
    }
    onChange();
  }, [rule, onChange]);
  const onValueChange = Object(react["useCallback"])(value => {
    rule.data = value;
    onChange();
  }, [rule, onChange]);
  const getRuleByType = Object(react["useCallback"])(() => {
    if (!rule.field || !chosenCondition || rule.condition === 'IS NULL' || rule.condition === 'IS NOT NULL') {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
    }
    if (chosenParam !== null && chosenParam !== void 0 && chosenParam.COMBO_INDICATOR) {
      let tempValue = undefined;
      if (Array.isArray(rule.data)) {
        tempValue = chosenParam.valid_values.filter(it => rule.data.indexOf(it.value) >= 0);
      } else {
        tempValue = chosenParam.valid_values.find(it => rule.data === it.value);
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
          name: `rule_number_${groupIndex}_${ruleIndex}`,
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
        name: `rule_text_${groupIndex}_${ruleIndex}`,
        title: '',
        onChange: onValueChange,
        value: rule.data,
        type: InputTypes.text,
        mandatory: true,
        width: '300px'
      });
    }
  }, [chosenParam, rule.data, rule.field, rule.condition, chosenCondition, onValueChange, onComboValueChange]);
  const removeGroup = Object(react["useCallback"])(index => {
    if (!parentGroup || !parentGroup.rules || parentGroup.rules.length === 0) {
      return;
    }
    let groupIndex = -1;
    if (typeof index === 'number') {
      groupIndex = index;
    } else {
      const splittedIndex = index.split('_');
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
      index: `${groupIndex}_${ruleIndex}`,
      group: rule.group,
      parameters: parameters
    }) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(RuleItemContainer, {
      children: [chosenParam !== null && chosenParam !== void 0 && chosenParam.DESCRIPTION ? /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_IconContainer, {
        title: (chosenParam === null || chosenParam === void 0 ? void 0 : chosenParam.DESCRIPTION) || '',
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(QueryBuilder_styles_Icon, {
          src: info_icon
        })
      }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
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
        onClick: () => removeGroup(ruleIndex),
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
  const {
    parent,
    group,
    parameters,
    index,
    onChange,
    removeGroup,
    type
  } = props;
  const {
    register,
    clearErrors,
    errors
  } = Object(react["useContext"])(TaskContext);
  const getRule = (parentGroup, rule, ruleIndex, lastRule) => {
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
  const addCondition = Object(react["useCallback"])(() => {
    group === null || group === void 0 ? void 0 : group.rules.push({
      condition: '',
      field: '',
      data: null,
      operator: 'AND',
      validValues: []
    });
    onChange();
  }, [onChange]);
  const addGroup = Object(react["useCallback"])(() => {
    group === null || group === void 0 ? void 0 : group.rules.push({
      group: {
        operator: 'AND',
        rules: []
      }
    });
    onChange();
  }, [onChange]);
  const removeGroupLocal = Object(react["useCallback"])(() => {
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
      children: group === null || group === void 0 ? void 0 : (_group$rules = group.rules) === null || _group$rules === void 0 ? void 0 : _group$rules.map((rule, index) => {
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
const getSubWhereInNotIn = (param, condition, value, type) => {
  if (!value || value.length === 0) {
    return '';
  }
  if (!Array.isArray(value)) {
    value = [value];
  }
  let operator = 'or';
  let equality = '=';
  if (condition !== 'IN') {
    operator = 'and';
    equality = '!=';
  }
  let result = '';
  for (let i = 0; i < value.length; i++) {
    if (type === 1) {
      result = result + `${param} ${equality} ${value[i]}`;
    } else {
      result = result + `'${value[i]}' ${equality} ANY(${param})`;
    }
    if (i < value.length - 1) {
      result = ` ${result} ${operator} `;
    }
  }
  return result;
};
const getSubQuery = (rule, parameters, type, resultValues, filter_types, isCoupling) => {
  if (!rule) {
    return '';
  }
  let field = '"' + rule.field + '"';
  if (type === 1) {
    field = rule.field || '';
  }
  let condition = rule.condition;
  let data = rule.data;
  if (type === 1) {
    if (rule.condition !== 'IS NULL' && rule.condition !== 'IS NOT NULL') {
      if (rule.original_type === 'TEXT') {
        data = data.replace(/\'/g, "''");
      }
      if (rule.condition === 'IN' || rule.condition === 'NOT IN') {
        const values = data.split(',').map(it => it.trim());
        data = [];
        values.forEach(value => {
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
    var _rule$type, _rule$type2;
    if (typeof data === 'string') {
      data = data.replace(/\'/g, "''");
    }
    data = "'" + data + "'";
    if (((_rule$type = rule.type) === null || _rule$type === void 0 ? void 0 : _rule$type.toLowerCase()) === "number" || ((_rule$type2 = rule.type) === null || _rule$type2 === void 0 ? void 0 : _rule$type2.toLowerCase()) === "integer") {
      if (!isCoupling) {
        field = "ARRAY(SELECT unnest(" + field + ")::numeric[] ) ";
      } else {
        field = field + "::numeric[] ";
      }
      data = rule.data;
    }
  }
  let table = rule.table;
  if (!table && parameters && parameters.length > 0) {
    const found = parameters.find(it => it.name === rule.field);
    if (found) {
      table = found.table;
    }
  }
  let prefix = `SELECT distinct ROOT_IID FROM ${table} WHERE `;
  if (type === 1) {
    prefix = ``;
  }
  if (rule.condition === 'IS NULL' || rule.condition === 'IS NOT NULL') {
    return '( ' + prefix + field + ' ' + condition + ' )';
  } else if (rule.condition === 'IN' || rule.condition === 'NOT IN') {
    if (type !== 1) {
      data = rule.data;
    }
    if (Array.isArray(data)) {
      const newData = [];
      data.forEach(it => {
        let value = it;
        if (typeof value === 'string') {
          value = value.replace(/\'/g, "''");
        }
        newData.push(value);
      });
      data = newData;
    } else if (typeof data === 'string') {
      data = data.split(',').map(it => it.trim());
    }
    return `( ${prefix} ${getSubWhereInNotIn(field, condition, data, type)})`;
  } else {
    if (type === 1) {
      return '( ' + prefix + field + ' ' + condition + ' ' + data + ' )';
    } else {
      return '( ' + prefix + data + ' ' + condition + ' ANY(' + field + ') )';
    }
  }
};
const computeQuery = (group, parametersList, type, resultValues, filter_types, isCoupling) => {
  if (!group) return '';
  let str = '(';
  for (let i = 0; i < group.rules.length; i++) {
    if (group.rules[i].group) {
      if (i === group.rules.length - 1) {
        str += computeQuery(group.rules[i].group, parametersList, type, resultValues, filter_types, isCoupling);
      } else {
        str += computeQuery(group.rules[i].group, parametersList, type, resultValues, filter_types, isCoupling) + ' ' + (group.rules[i].operator === 'AND' ? 'INTERSECT' : 'UNION') + ' ';
      }
    } else {
      let data;
      if (!group.rules[i].data && group.rules[i].data !== '' && group.rules[i].condition !== 'IS NULL' && group.rules[i].condition !== 'IS NOT NULL') {
        return '';
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
        const paramFound = parametersList === null || parametersList === void 0 ? void 0 : parametersList.find(it => it.name === group.rules[i].field);
        if (paramFound && paramFound.valid_values) {
          const validValues = paramFound.valid_values.map(it => it.label);
          if (validValues && validValues.length > 0 && validValues.indexOf(group.rules[i].data) < 0 && group.rules[i].condition !== 'IS NULL' && group.rules[i].condition !== 'IS NOT NULL') {
            return '';
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
        str += getSubQuery(group.rules[i], parametersList, type, resultValues, filter_types, isCoupling);
      } else {
        str += getSubQuery(group.rules[i], parametersList, type, resultValues, filter_types, isCoupling);
        if (type === 1) {
          str += ' \n' + group.rules[i].operator + ' \n';
        } else {
          str += ' \n' + (group.rules[i].operator === 'AND' ? 'INTERSECT' : 'UNION') + ' \n';
        }
      }
    }
  }
  return str + ')';
};
const getSelectionParamValue = (filter, parametersList, isCoupling, type) => {
  const resultValues = [];
  const filter_types = [];
  let validStatement = false;
  const checkRule = (rule, type) => {
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
  const checkGroup = (group, type, first) => {
    if (!group.operator) {
      return false;
    }
    for (let i = 0; i < group.rules.length; i++) {
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
    const result = computeQuery(filter.group, parametersList, type, resultValues, filter_types, isCoupling);
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

const duration = "2s forwards";
function ChangeBorderColor(property, color) {
  let animation = `to {
    ${property}: ${color};
  }`;
  return styled_components_browser_esm["c" /* keyframes */]`
    ${animation}
  `;
}
const TaskMainWidget_styles_Container = styled_components_browser_esm["b" /* default */].div`
  width: 876px;
  height: 277px;
  margin: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  user-select: none;
  position: absolute;
  top: -37px;
  scale: 0.7;
    @media (max-width: 1400px) {
    margin-left: calc(20% + (1400px - 100vw) / 2);
  }
`;
const ArrowTriangle = styled_components_browser_esm["b" /* default */].div`
  border-top: 19px solid transparent;
  border-bottom: 19px solid transparent;
  border-left: 19px solid #ccc;
    animation: ${_ref => {
  let {
    color
  } = _ref;
  return ChangeBorderColor('border-left-color', color || "#ccc");
}} ${duration};
  position: absolute;
  top: 50%;
  left: ${props => props.left};
  transform: translate(-50%, -50%);
  z-index: 1;
`;
const TaskMainWidget_styles_Title = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 19px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  letter-spacing: normal;
  text-align: center;
  color: var(--disabled);
  ${_ref2 => {
  let {
    color
  } = _ref2;
  return color && styled_components_browser_esm["a" /* css */]`
      animation: ${ChangeBorderColor('color', color)} ${duration};
    `;
}}
  ${_ref3 => {
  let {
    showEllipsis
  } = _ref3;
  return showEllipsis && `
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  width: 100%;
    `;
}}

`;
const SubTitle = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 15px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  letter-spacing: normal;
  text-align: center;
  color: ${props => props.color ? props => props.color : "#ccc"};
`;
const Empty = styled_components_browser_esm["b" /* default */].div`
  height: ${_ref4 => {
  let {
    height
  } = _ref4;
  return height || "14px";
}};
`;
const Img = styled_components_browser_esm["b" /* default */].img``;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/styles.ts


const styles_duration = "2s forwards";
const infinite = "2s infinite";
function styles_ChangeBorderColor(property, color) {
  let animation = `to {
    ${property}: ${color};
  }`;
  return styled_components_browser_esm["c" /* keyframes */]`
    ${animation}
  `;
}
function blinking(highlightColor) {
  let animation = "";
  if (highlightColor === "blue") {
    animation = `
    0% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
    25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
    50% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
    75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
    100% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
    `;
  }
  if (highlightColor === "purple") {
    animation = `
      0% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
      25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
      50% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
      75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
      100% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
      `;
  }
  return styled_components_browser_esm["c" /* keyframes */]`
    ${animation}
  `;
}
const RectangleWrapper = styled_components_browser_esm["d" /* styled */].div`
  position: relative;
  width: 165px;
  height: 165px;
`;
const ContentWrapper = styled_components_browser_esm["d" /* styled */].div`
    width: 100%;
    height: 100%;
    border-radius: inherit;
    background-color: var(--white);
    animation: ${_ref => {
  let {
    backgrounColor
  } = _ref;
  return styles_ChangeBorderColor('background-color', backgrounColor);
}} ${styles_duration};
    display: flex;
    flex-direction: column;
    ${_ref2 => {
  let {
    centerTitle
  } = _ref2;
  return centerTitle ? `
    justify-content: space-between;
    ` : `

    justify-content: center;
    `;
}}
    padding: ${props => props.padding};
    box-sizing: border-box;
    align-items: center;
    box-shadow:${props => props.boxShadow};
    border: solid 4px
      ${props => props.innerBorderColor};
  `;
const Rectangle = styled_components_browser_esm["d" /* styled */].div`
  width: 165px;
  height: 165px;
  padding: 12px;
  border-radius: 20px;
  border: solid 5px var(--disabled);
  ${_ref3 => {
  let {
    highlightColor,
    outerBorderColor
  } = _ref3;
  return highlightColor === "none" ? styled_components_browser_esm["a" /* css */]` animation: ${styles_ChangeBorderColor('border-color', outerBorderColor)} ${styles_duration};` : styled_components_browser_esm["a" /* css */]` animation: ${blinking(highlightColor)} ${infinite};
            border-color: ${outerBorderColor};
     `;
}};
  position: absolute;
  z-index: 100;
  box-sizing: border-box;
  user-select: none;
  ${_ref4 => {
  let {
    status
  } = _ref4;
  return status && status !== StatusEnum.disabled && `
    cursor: pointer;
  `;
}}
`;
const InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div`
    position: absolute;
    z-index: 100;
    top: -26px;
    right: -10px;
    cursor: auto;
`;
const SubTitleWrapper = styled_components_browser_esm["d" /* styled */].div`
  height: 28px;
  display: flex;
  align-items: end;
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/services.ts

const getIndicatorColors = (status, name) => {
  let primaryColor = name === "target" ? "var(--sky-blue)" : name === "test_data_store" ? "#8146f0" : "var(--lovelyPurple)";
  let secondaryColor = name === "target" ? "var(--sky-blue)" : name === "test_data_store" ? "#2c75f2" : "var(--lovelyPurple)";
  let borderColor = `linear-gradient(to right,${primaryColor},${secondaryColor})`;
  let dotsColor = primaryColor;
  let dotsBackground = "linear-gradient(to right, #FFFFFF, #FFFFFF)";
  if (status === StatusEnum.completed) {
    dotsBackground = `linear-gradient(to right,${primaryColor},${secondaryColor})`;
  }
  return {
    borderColor: borderColor,
    dotsColor: dotsColor,
    dotsBackground: dotsBackground
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/MenuIconSVG.tsx


function MenuIconSVG(props) {
  const {
    color
  } = props;
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

const CicleMenuIconWrapper = styled_components_browser_esm["d" /* styled */].div`
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  position: absolute;
  margin-left: auto;
  margin-right: auto;
  right: 0;
  left: 0;
  bottom: -21px;
`;
const MenuIconGradiant = styled_components_browser_esm["d" /* styled */].div`
    background-image: ${props => props.borderBackgroundImage ? props.borderBackgroundImage : "none"};
    border-radius: 50%;
    width: 30px;
    height: 30px;
`;
const IndicatorContent = styled_components_browser_esm["d" /* styled */].div`
  position: absolute;
  width: 24px;
  height: 24px;
  background-image: ${props => props.dotsBackground ? props.dotsBackground : "none"};
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  top: 3px;
  left: 3px;
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Indicator/index.tsx








function Indicator(props) {
  const {
    status,
    name
  } = props;
  const [colors, setColors] = Object(react["useState"])(() => getIndicatorColors(status, name));
  Object(react["useEffect"])(() => {
    setColors(() => getIndicatorColors(status, name));
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

const styles_TooltipContainer = styled_components_browser_esm["b" /* default */].div`
  position: relative;
  display: inline-block;

  .tooltip-text {
    display: none;
    position: absolute;
    ${_ref => {
  let {
    hideTriangle
  } = _ref;
  return !hideTriangle ? `border-radius:3px;
      border: solid 1px #ccc;
      background-color: #fff;
      ` : `
       border-radius:3px;
       border: solid 1px #ccc;
       background: #fff;
      `;
}}
    padding: 10px;
    font-family: Roboto;
    font-size: 17px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    z-index: 101;
    width: max-content;
    max-width: 250px;
    max-height: 400px;
    top: -11px;
    ${_ref2 => {
  let {
    position
  } = _ref2;
  return position === 'left' ? `
      right: calc(100% + 10px);
    ` : `
      left: calc(100% + 10px);
    `;
}}

    &:before {
      content: '';
      position: absolute;
      ${_ref3 => {
  let {
    hideTriangle
  } = _ref3;
  return !hideTriangle ? `display:block;` : `display:none;`;
}}
      border-style: solid;
      border-width: 5px;
      z-index: 102;

      ${_ref4 => {
  let {
    position
  } = _ref4;
  return position === 'left' ? `
        right: -10px;
        top: 50%;
        transform: translateY(-50%);
        border-color: transparent transparent transparent #FFFFFF;
      ` : `
        left: -10px;
        top: 50%;
        transform: translateY(-50%);
        border-color: transparent  #FFFFFF transparent transparent;
      `;
}}
    }

    &:after {
      content: '';
      position: absolute;
      ${_ref5 => {
  let {
    hideTriangle
  } = _ref5;
  return !hideTriangle ? `display:block;` : `display:none;`;
}}
      border-style: solid;
      border-width: 6px;
      z-index: -1;

      ${_ref6 => {
  let {
    position
  } = _ref6;
  return position === 'left' ? `
        right: -12px;
        top: 50%;
        transform: translateY(-50%);
        border-color: transparent transparent transparent #ccc;;
      ` : `
        left: -12px;
        top: 50%;
        transform: translateY(-50%);
        border-color: transparent #ccc transparent transparent;
      `;
}}
    }
  }
  &:hover .tooltip-text {
    display: block;
  }
`;
const UL = styled_components_browser_esm["b" /* default */].ul`
  margin: 0;
  padding: 0 0 0 12px;
`;
const LI = styled_components_browser_esm["b" /* default */].li`
  padding:0 !important;
  padding-bottom: 4px !important;
  background-color: transparent !important;
`;
// CONCATENATED MODULE: ./src/components/Tooltip/index.tsx
// Tooltip.tsx



const Tooltip = _ref => {
  let {
    children,
    position,
    textArray,
    hideTriangle
  } = _ref;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TooltipContainer, {
    position: position,
    hideTriangle: hideTriangle,
    children: [children, /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
      className: "tooltip-text",
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])("div", {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(UL, {
          children: textArray.map(text => {
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(LI, {
              children: text
            });
          })
        })
      })
    })]
  });
};
/* harmony default export */ var components_Tooltip = (Tooltip);
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/services.tsx

const getColors = (status, isTarget, isSelected, isConnectionColored) => {
  let primaryColor = isTarget ? 'var(--sky-blue)' : 'var(--lovelyPurple)';
  let boxShadow = 'none';
  let subTitleColor = 'var(--disabled)';
  let titleColor = 'var(--disabled)';
  let backgroundColor = 'var(--white)';
  let innerBorderColor = 'transparent';
  let outerBorderColor = 'var(--disabled)';
  let arrowTriangleColor = 'var(--disabled)';
  let highlightColor = 'none';
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
    innerBorderColor,
    outerBorderColor,
    backgroundColor,
    titleColor,
    subTitleColor,
    boxShadow,
    arrowTriangleColor,
    highlightColor
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/SourceAndEnv/index.tsx









function SourceAndEnv(props) {
  const {
    title,
    status,
    onClick,
    isSelected,
    info,
    id,
    isTarget,
    subTitle,
    placeHolder,
    subTitlePlaceHolder,
    isConnectionColored
  } = props;
  const [colors, setColors] = Object(react["useState"])(() => getColors(status, isTarget, isSelected, isConnectionColored));
  Object(react["useEffect"])(() => {
    setColors(() => getColors(status, isTarget, isSelected, isConnectionColored));
  }, [status, isSelected, isTarget, isConnectionColored]);
  const handleClick = Object(react["useCallback"])(event => {
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
      left: `${isTarget ? '-84%;' : '103%'}`,
      color: colors.arrowTriangleColor
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(react["Fragment"], {})]
  });
}
const MemoizedDataSource = /*#__PURE__*/Object(react["memo"])(SourceAndEnv);
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/services.ts

const getTestDataStoreColors = (status, isSelected) => {
  let boxShadow = "none";
  let subTitleColor = "var(--disabled)";
  let titleColor = "var(--disabled)";
  let backgroundColor = "var(--white)";
  let outerBorderBackgroundImage = "linear-gradient(to right, var(--disabled), var(--disabled))";
  let innerBorderBackgroundImage = "linear-gradient(to right,var(--white), var(--white))";
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
  const {
    color
  } = props;
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
  const {
    color
  } = props;
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


const TestDataStore_styles_duration = "1s forwards";
function TestDataStore_styles_ChangeBorderColor(property, color) {
  let animation = `to {
    ${property}: ${color};
  }`;
  return styled_components_browser_esm["c" /* keyframes */]`
    ${animation}
  `;
}
const CicleContentWrapper = styled_components_browser_esm["d" /* styled */].div`
    width: 216px;
    height: 216px;
    border-radius: inherit;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    background:var(--white);
    ${_ref => {
  let {
    backgrounColor
  } = _ref;
  return backgrounColor && styled_components_browser_esm["a" /* css */]`
      animation: ${TestDataStore_styles_ChangeBorderColor('background', backgrounColor)} ${TestDataStore_styles_duration};
    `;
}}
    padding: ${props => props.padding};
    box-sizing: border-box;
    align-items: center;
    position: absolute;
      top: 16px;
      left: 16px;
  `;
const CircleGradiantWrapper = styled_components_browser_esm["d" /* styled */].div`
position: absolute;
z-index: 2;
`;
const CircleGradiant = styled_components_browser_esm["d" /* styled */].div`
background: var(--disabled);
${_ref2 => {
  let {
    borderBackgroundImage
  } = _ref2;
  return borderBackgroundImage && styled_components_browser_esm["a" /* css */]`
     animation: ${TestDataStore_styles_ChangeBorderColor('background', borderBackgroundImage)} ${TestDataStore_styles_duration};
  `;
}}
border-radius: 50%;
width: 258px;
height: 258px;
`;
const CircleContentGradiant = styled_components_browser_esm["d" /* styled */].div`
    background:transparent;
    ${_ref3 => {
  let {
    borderBackgroundImage
  } = _ref3;
  return borderBackgroundImage && styled_components_browser_esm["a" /* css */]`
      animation: ${TestDataStore_styles_ChangeBorderColor('background', borderBackgroundImage)} ${TestDataStore_styles_duration};
    `;
}}
    box-shadow:${props => props.boxShadow};
    border-radius: 50%;
    width: 224px;
    height: 224px;
`;
const Circle = styled_components_browser_esm["d" /* styled */].div`
    width: 248px;
    height: 248px;
  padding: 12px;
  position: relative;
  border-radius: 50%;
  box-sizing: border-box;
  background-color:#f2f2f2;
  position: absolute;
  top: 5px;
    left: 5px;
  ${_ref4 => {
  let {
    status
  } = _ref4;
  return status && status !== StatusEnum.disabled && `
    cursor: pointer;
  `;
}}
    box-shadow:${props => props.boxShadow};
`;
const TestDataStoreWrapper = styled_components_browser_esm["d" /* styled */].div`
 position:relative;
 width:258px;
 height:258px
`;
const styles_InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div`
    position: absolute;
    top: 10px;
    right: 20px;
    cursor: auto;
    z-index: 102;
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/TestDataStore/index.tsx











function TestDataStore(props) {
  const {
    status,
    onClick,
    isSelected,
    info
  } = props;
  const [colors, setColors] = Object(react["useState"])(() => getTestDataStoreColors(status, isSelected));
  Object(react["useEffect"])(() => {
    setColors(() => getTestDataStoreColors(status, isSelected));
  }, [status, isSelected]);
  const handleClick = Object(react["useCallback"])(event => {
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


const FilterWrapper = styled_components_browser_esm["d" /* styled */].div`
cursor: pointer;
`;
const EntityIconWrapper = Object(styled_components_browser_esm["d" /* styled */])(SubTitle)`
   position: absolute;
   left: 0;
   right: 0;
   bottom: 25px;
`;
const FilterIconWrapper = styled_components_browser_esm["d" /* styled */].div`
    width: 31px;
    height: 31px;
    border: solid 2px ${props => props.outerBorderColor};
    background-color:${props => props.backgroundColor};
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    position: absolute;
    padding-top: 3px;
    right: 0;
    left: 0;
    bottom: 0;
    top: 0;
    margin: auto;
    animation: ${props => {
  if (props.highlightColor === "blue") {
    return styled_components_browser_esm["c" /* keyframes */]`
      0% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
      25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
      50% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
      75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
      100% {box-shadow:  0px 0px 0px 10px rgba(20, 131, 243, 0.2);}
      `;
  }
  if (props.highlightColor === "purple") {
    return styled_components_browser_esm["c" /* keyframes */]`
        0% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
        25% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
        50% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
        75% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0);}
        100% {box-shadow:  0px 0px 0px 10px rgba(132,68,240, 0.2);}
        `;
  }
  return "";
}} 2s infinite;
`;
const SubsetTitle = Object(styled_components_browser_esm["d" /* styled */])(SubTitle)`
   position: absolute;
   left: 0;
   right: 0;
   top: 28px;
`;
const styles_Img = styled_components_browser_esm["d" /* styled */].img`

`;
const Filter_styles_InfoIconWrapper = styled_components_browser_esm["d" /* styled */].div`
    position: relative;
`;
const InfoIconContainer = styled_components_browser_esm["d" /* styled */].div`
    position: absolute;
    top: -44px;
    left: 48px;
    cursor: auto;
    z-index: 101;
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/FilterIconSVG.tsx


function FilterIcon(props) {
  const {
    color
  } = props;
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

const getSubsetColors = (subsetStatus, isDataSourceSubSet, isSelected) => {
  let primaryColor = isDataSourceSubSet ? 'var(--lovelyPurple)' : 'var(--sky-blue)';
  let primaryHighlightColor = isDataSourceSubSet ? 'purple' : 'blue';
  let color = 'var(--white)';
  let fontColor = 'var(--black-title)';
  let subsetBorderColor = primaryColor;
  let highlightColor = 'none';
  let backgroundColor = color;
  let iconColor = primaryColor;
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
    subsetBorderColor,
    highlightColor,
    backgroundColor,
    iconColor,
    fontColor
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Filter/index.tsx









function Filter_Filter(props) {
  const {
    subsetStatus,
    isDataSourceSubSet,
    onClick,
    info,
    isSelected
  } = props;
  const [colors, setColors] = Object(react["useState"])(() => getSubsetColors(subsetStatus, isDataSourceSubSet, isSelected));
  Object(react["useEffect"])(() => {
    setColors(() => getSubsetColors(subsetStatus, isDataSourceSubSet, isSelected));
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
        id: `${isDataSourceSubSet ? 'source_data_subset' : 'target_data_subset'}`,
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

const dash = styled_components_browser_esm["c" /* keyframes */]`
0% {
  background-position: 0px 0px, 600px 116px, 0px 150px, 416px 0px;
}
100% {
  background-position: 600px 0px, 0px 116px, 0px 0px, 416px 150px;
}
`;
const DashedHrWrapper = styled_components_browser_esm["d" /* styled */].div`
  display: flex;
  flex-direction: row;
  height: 5px;
`;
const DashedHr = styled_components_browser_esm["d" /* styled */].div`
  background-image: linear-gradient(
    90deg,
    ${props => props.lineColor} 50%,
    transparent 50%
  );
  background-size: 40px 10px, 40px 10px, 40px 10px, 40px 10px;
  animation: ${props => props.animation ? styled_components_browser_esm["a" /* css */]`
          ${dash} 10s infinite linear
        ` : "none"};
  width: 100%;
  height: 100%;
  display:${_ref => {
  let {
    display
  } = _ref;
  return display;
}}
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/DashedHr/services.tsx
const getDashedHrColors = (isDataSourceSubSet, isConnectionColored) => {
  let animation = false;
  let primaryColor = 'var(--disabled)';
  let secondaryColor = 'var(--disabled)';
  let color = isDataSourceSubSet ? 'var(--lovelyPurple)' : 'var(--sky-blue)';
  let display = isConnectionColored === undefined ? 'none' : 'block';
  if (isConnectionColored) {
    primaryColor = color;
    secondaryColor = 'var(--lovelyPurple)';
    animation = true;
  }
  return {
    primaryColor,
    secondaryColor,
    animation,
    display
  };
};
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/DashedHr/index.tsx




function Subset(props) {
  const {
    isDataSourceSubSet,
    isConnectionColored
  } = props;
  const [colors, setColors] = Object(react["useState"])(() => getDashedHrColors(isDataSourceSubSet, isConnectionColored));
  Object(react["useEffect"])(() => {
    setColors(() => getDashedHrColors(isDataSourceSubSet, isConnectionColored));
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

const SubsetWrapper = styled_components_browser_esm["d" /* styled */].div`
    position: relative;
    width: calc((100% - (165px + 165px + 264px + 28px))/2);
    padding-left: 14px;
`;
// CONCATENATED MODULE: ./src/components/task/TaskMainWidget/Subset/index.tsx





function Subset_Subset(props) {
  const {
    subsetStatus,
    dataStatus,
    isDataSourceSubSet,
    onClick,
    info,
    isSelected,
    isConnectionColored,
    subsetType
  } = props;
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







let StatusEnum = /*#__PURE__*/function (StatusEnum) {
  StatusEnum["disabled"] = "disabled";
  StatusEnum["enabled"] = "enabled";
  StatusEnum["completed"] = "completed";
  StatusEnum["partial"] = "partial";
  StatusEnum["selected"] = "selected";
  StatusEnum["blink"] = "blink";
  return StatusEnum;
}({});
let SubsetPossition = /*#__PURE__*/function (SubsetPossition) {
  SubsetPossition["source"] = "source";
  SubsetPossition["target"] = "target";
  SubsetPossition["undefined"] = "";
  return SubsetPossition;
}({});
function TaskMainWidget(props) {
  const {
    onClick,
    data,
    source_environment_name,
    environment_name,
    targetInfo,
    sourceInfo,
    subsetInfo,
    datastoreInfo,
    selectedStep,
    subsetType,
    sourceSubTitle,
    targetSubTitle
  } = props;
  //  const { onClick, source_environment_name, environment_name, targetInfo,subsetType,sourceSubTitle,targetSubTitle} = props;
  const handleClickOnStep = Object(react["useCallback"])(event => {
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

  const {
    targetSubsetStatus,
    sourceSubsetStatus
  } = Object(react["useMemo"])(() => {
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
  }, [data.subsetStatus, data.subsetPosition]);
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









function Parameters(props) {
  const {
    register,
    clearErrors,
    errors,
    unregister,
    resetField,
    taskData,
    statusesFuncMap,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    be_id,
    environment_id,
    source_environment_name,
    environment_name,
    parameters,
    selection_method,
    selection_param_value,
    filterout_reserved,
    clone_ind,
    replace_sequences,
    load_entity,
    target_env,
    sync_mode,
    version_ind,
    enable_param_lu_name,
    isCoupling
  } = taskData;
  const [entitiesCount, setEntitiesCount] = Object(react["useState"])(0);
  const [loading, setLoading] = Object(react["useState"])(false);
  const [filterReserveError, setFilterReserveError] = Object(react["useState"])(false);
  const [parametersList, setParametersList] = Object(react["useState"])(null);
  const [filter, setFilter] = Object(react["useState"])({
    group: {
      rules: [],
      operator: 'AND'
    }
  });
  Object(react["useEffect"])(() => {
    let filter = undefined;
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
  Object(react["useEffect"])(() => {
    // Create a variable to hold the timeout
    let timeoutId;
    // Set the timeout to delay the API call by 1.5 seconds
    timeoutId = setTimeout(() => {
      getEntitesCount();
    }, 3500);

    // Cleanup function to clear the timeout if useEffect is called again before 1.5 seconds
    return () => clearTimeout(timeoutId);
  }, [selection_param_value, filterout_reserved, filter]);
  Object(react["useEffect"])(() => {
    const getData = async () => {
      if (!be_id || !source_environment_name && !environment_name) {
        return;
      }
      const sourceStatus = statusesFuncMap['dataSourceStatus'](taskData);
      let env_name = (sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name) || '';
      let data = await apis_task.getParameters(be_id, env_name);
      const result = [];
      Object.keys(data).forEach(key => {
        const value = data[key];
        result.push({
          label: enable_param_lu_name ? key : key.split('.')[1],
          value: key,
          param_name: key,
          name: value.PARAM_NAME,
          table: value.LU_PARAMS_TABLE_NAME,
          param_type: value.PARAM_TYPE,
          COMBO_INDICATOR: value.COMBO_INDICATOR === 'true',
          DESCRIPTION: value.DESCRIPTION,
          valid_values: Array.isArray(value['VALID_VALUES']) ? value['VALID_VALUES'].map(it => ({
            label: it,
            value: it
          })) : value['VALID_VALUES'],
          min_value: value.PARAM_TYPE === 'REAL' || value.PARAM_TYPE === 'INTEGER' || value.PARAM_TYPE === 'NUMBER' ? parseFloat(value['MIN_VALUE']) : 0,
          max_value: value.PARAM_TYPE === 'REAL' || value.PARAM_TYPE === 'INTEGER' || value.PARAM_TYPE === 'NUMBER' ? parseFloat(value['MAX_VALUE']) : 0
        });
      });
      setParametersList(result);
    };
    getData();
    if (filterout_reserved && !environment_id) {
      saveForm({
        filterout_reserved: 'OTHERS'
      });
      setFilterReserveError(true);
    }
  }, []);
  const parametersDataChange = Object(react["useCallback"])(() => {
    setFilter({
      ...filter
    });
    const selection_param_value = getSelectionParamValue(filter, parametersList, isCoupling);
    saveForm({
      parameters: JSON.stringify(filter),
      selection_param_value
    });
  }, [filter, saveForm, parametersList, isCoupling]);
  const parametersRandomChange = Object(react["useCallback"])(value => {
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

  const getEntitesCount = Object(react["useCallback"])(() => {
    if (!selection_param_value || selection_param_value === '()') {
      var _filter$group, _filter$group$rules;
      if ((filter === null || filter === void 0 ? void 0 : (_filter$group = filter.group) === null || _filter$group === void 0 ? void 0 : (_filter$group$rules = _filter$group.rules) === null || _filter$group$rules === void 0 ? void 0 : _filter$group$rules.length) === 0) {
        setEntitiesCount(0);
      }
      return;
    }
    const getData = async () => {
      if (!be_id || !source_environment_name && !environment_name) {
        return;
      }
      setLoading(true);
      let local_filterout_reserved = filterout_reserved;
      if ((clone_ind || replace_sequences) && load_entity || target_env === 'ai_training' || !environment_id || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL') {
        local_filterout_reserved = 'NA';
      }
      const sourceStatus = statusesFuncMap['dataSourceStatus'](taskData);
      const body = {
        where: selection_param_value,
        tar_env_name: environment_name,
        src_env_name: sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name,
        queryJson: JSON.stringify(filter),
        filterout_reserved: local_filterout_reserved || 'NA'
      };
      console.log(body);
      try {
        const data = await apis_task.getEntitiesCount(be_id, (sourceStatus === StatusEnum.disabled ? environment_name : source_environment_name) || '', body);
        setEntitiesCount(data);
      } catch (err) {
        setEntitiesCount(0);
      } finally {
        setLoading(false);
      }
    };
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

const Tables_styles_Container = styled_components_browser_esm["b" /* default */].div`
    min-width: 60vw;
    position: relative;
    display: flex;
    gap: 10px;
`;
const styles_DateFormatNote = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #666;
`;
const Tables_styles_LeftSide = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    width: 300px;
    padding-right: 30px;
    border-right: 1px solid #ccc;
`;
const Tables_styles_RightSide = styled_components_browser_esm["b" /* default */].div`
    width: calc(100% - 330px);
`;
const Tables_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 15px;
`;
const styles_TablesContainer = styled_components_browser_esm["b" /* default */].div`
`;
const TableHeader = styled_components_browser_esm["b" /* default */].div`
    padding-bottom: 12px;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    border-bottom: solid 2px #ccc;
`;
const TableBody = styled_components_browser_esm["b" /* default */].div`
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    max-height: 270px;
    overflow: auto;
`;
const Tables_styles_TableRow = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    padding: 10px 10px 10px 0px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: solid 1px #ccc;
`;
const TableName = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
`;
const Tables_styles_Actions = styled_components_browser_esm["b" /* default */].div`

`;
const Tables_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const TableFooter = styled_components_browser_esm["b" /* default */].div`
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    background-color: #f2f2f2;
    padding: 10px;
`;
// CONCATENATED MODULE: ./src/images/filter.svg
/* harmony default export */ var images_filter = ("js/dist/2a0f0e6b15e81eeaa08daf2e86b5b6dc.svg");
// CONCATENATED MODULE: ./src/images/filter-after-change.svg
/* harmony default export */ var filter_after_change = ("js/dist/2cdcea9cad68e0fe239c57764a88c083.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/DataSubset/Tables/index.tsx









function TableSubset(props) {
  const {
    register,
    clearErrors,
    errors,
    unregister,
    resetField,
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    source_environment_name,
    environment_name,
    parameters,
    tableList,
    subsetReset,
    isCoupling
  } = taskData;
  const [selectedTable, setSelectedTable] = Object(react["useState"])('');
  Object(react["useEffect"])(() => {
    if (subsetReset) {
      setSelectedTable('');
      saveForm({
        subsetReset: false
      });
    }
  }, [subsetReset]);
  const [parametersList, setParametersList] = Object(react["useState"])(null);
  const [filter, setFilter] = Object(react["useState"])({
    group: {
      rules: [],
      operator: 'AND'
    }
  });

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

  Object(react["useEffect"])(() => {
    const getData = async () => {
      if (!selectedTable) {
        return;
      }
      const tableData = (tableList || []).find(it => it.reference_table_name === selectedTable);
      if (!tableData) {
        return;
      }
      let data = await apis_task.getTableParameters(tableData.interface_name, tableData.schema_name, tableData.reference_table_name);
      const result = [];
      data.forEach(item => {
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
          DESCRIPTION: '',
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
    };
    getData();
  }, [selectedTable]);
  const parametersDataChange = Object(react["useCallback"])(() => {
    const tableData = (tableList || []).find(it => it.reference_table_name === selectedTable);
    if (!tableData) {
      return;
    }
    tableData.gui_filter = JSON.stringify(filter);
    tableData.filter_type = 'SQL';
    const sqlQueryData = getSelectionParamValue(filter, parametersList, isCoupling, 1);
    tableData.table_filter = sqlQueryData.sqlQuery === '()' ? null : sqlQueryData.sqlQuery;
    tableData.filter_parameters = sqlQueryData.values;
    tableData.filter_fields = sqlQueryData.filter_types;
    saveForm({
      tableList: [...(tableList || [])]
    });
  }, [saveForm, tableList, selectedTable, filter]);
  const getTableRows = Object(react["useCallback"])(() => {
    return (tableList || []).map(it => /*#__PURE__*/Object(jsx_runtime["jsxs"])(Tables_styles_TableRow, {
      onClick: () => setSelectedTable(it.reference_table_name),
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TableName, {
        title: `Interface: ${it.interface_name}, schema: ${it.schema_name}.`,
        children: it.reference_table_name
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Actions, {
        children: it.table_filter && it.table_filter !== '()' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Icon, {
          src: filter_after_change
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(Tables_styles_Icon, {
          src: images_filter
        })
      })]
    }));
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

const FilterOutReserved_styles_Container = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 15px;
`;
const FilterOutReserved_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 7px;
`;
const FilterOutReserved_styles_Body = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    gap: 10px;
`;
// CONCATENATED MODULE: ./src/components/FilterOutReserved/index.tsx





function FilterOutReserved() {
  const {
    register,
    clearErrors,
    errors,
    unregister,
    resetField,
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    filterout_reserved,
    replace_sequences,
    clone_ind,
    target_env,
    environment_id,
    load_entity,
    sync_mode,
    version_ind,
    selection_method
  } = taskData;
  const onChange = Object(react["useCallback"])(value => {
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
        onChange: () => onChange('OTHERS'),
        name: "select_filter_out_reserved",
        value: 'OTHERS',
        selectedValue: '' + filterout_reserved,
        title: 'Reserved by others',
        disabled: (clone_ind || replace_sequences) && load_entity || target_env === 'ai_training' || !(sync_mode === 'OFF' && version_ind) && selection_method === 'ALL'
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
        onChange: () => onChange('ALL'),
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
















let DataSubsetTypeEnum = /*#__PURE__*/function (DataSubsetTypeEnum) {
  DataSubsetTypeEnum["Entity"] = "Entity";
  DataSubsetTypeEnum["Tables"] = "Tables";
  return DataSubsetTypeEnum;
}({});
let SelectionMethodEnum = /*#__PURE__*/function (SelectionMethodEnum) {
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
  const {
    icon,
    text
  } = props;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(DatasetIconContainer, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(DataSubset_styles_Icon, {
      src: icon
    }), text]
  });
}
const entitySeletionMethods = [{
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
  const {
    register,
    clearErrors,
    errors,
    unregister,
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    dataSubsetType,
    selection_method,
    version_ind,
    sync_mode,
    generation_type,
    dataSourceType,
    synthetic_type,
    source_type,
    userRole,
    sourceUserRole,
    isCoupling,
    maxToCopy
  } = taskData;
  const [localSelectionMethod, setLocalSelectionMethod] = Object(react["useState"])(null);
  Object(react["useEffect"])(() => {
    if (selection_method === 'TABLES') {
      return;
    }
    const updateData = {};
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
  const entitySelectionMethodOptions = Object(react["useMemo"])(() => {
    let result = entitySeletionMethods;
    let changedLabel = false;
    if (sync_mode === 'OFF' && version_ind) {
      result = result.filter(it => it.value === 'L' || it.value === 'ALL');
      const found = result.find(it => it.value === 'ALL');
      if (found) {
        changedLabel = true;
        found.label = 'Select all entities of the selected version';
      }
    } else if (dataSourceType !== 'data_source' && synthetic_type === 'generated_data') {
      result = result.filter(it => it.value !== 'L' && it.value !== 'ALL');
      if (!isCoupling && dataSourceType === 'ai_generated') {
        result = result.filter(it => it.value !== 'P');
      }
    }
    if (!((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' || (!userRole || userRole !== null && userRole !== void 0 && userRole.allowed_random_entity_selection) && (!sourceUserRole || sourceUserRole !== null && sourceUserRole !== void 0 && sourceUserRole.allowed_random_entity_selection) && (userRole || sourceUserRole))) {
      result = result.filter(it => it.value !== 'R');
    }
    if (sync_mode !== 'OFF' || !version_ind) {
      if ((systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'tester' && (maxToCopy || 0) < 9007199254740992) {
        result = result.filter(it => it.value !== 'ALL');
      }
    }
    const found = result.find(it => it.value === 'ALL');
    if (found && !changedLabel) {
      found.label = 'Predefined entity list';
    }
    return result;
  }, [sync_mode, version_ind, dataSourceType, synthetic_type, userRole, sourceUserRole, systemUserRole]);
  Object(react["useEffect"])(() => {
    // TODO Save Data
    let temp = selection_method || 'L';
    if (selection_method === 'PR') {
      temp = 'P';
    }
    const found = entitySelectionMethodOptions.find(it => it.value === temp);
    if (found) {
      setLocalSelectionMethod(found);
    }
  }, [selection_method, entitySelectionMethodOptions]);
  const getSelectionMethodBody = Object(react["useCallback"])(() => {
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
  const selectionMethodChange = Object(react["useCallback"])(item => {
    setLocalSelectionMethod(item);
    unregister('selection_param_value');
    saveForm({
      selection_method: item.value,
      selection_param_value: undefined,
      num_of_entities: undefined
    });
  }, [saveForm]);
  const generationTypeChange = Object(react["useCallback"])(value => {
    saveForm({
      generation_type: value
    });
  }, [saveForm]);
  const parametersRandomChange = Object(react["useCallback"])(value => {
    saveForm({
      selection_method: value ? 'PR' : 'P'
    });
  }, [saveForm]);
  const getSelectionMethodSelectRightSide = Object(react["useCallback"])(() => {
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
              title: 'Max number of entities',
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

const TestDataStore_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: center;
`;
const TestDataStore_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display:flex;
    flex-direction: column;
    align-items:flex-start;
    gap: 33px;
    justify-content: space-between;
`;
const RefreshDataContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 24px;
    display: flex;
    flex-direction: column;
    gap: 19px;
`;
const RetentionPeriodContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
`;
const styles_DataVersioningContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 33px;
    display: flex;
    align-items: center;
    gap: 20px;
`;
const TestDataStore_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    color: #2e2e2e;
`;
// CONCATENATED MODULE: ./src/components/Periods/styles.ts

const Periods_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 20px;
    align-items: flex-end;
`;
// CONCATENATED MODULE: ./src/containers/Task/Main/usePeriods.ts




let PeriodUnitType = /*#__PURE__*/function (PeriodUnitType) {
  PeriodUnitType["Minutes"] = "Minutes";
  PeriodUnitType["Hours"] = "Hours";
  PeriodUnitType["Days"] = "Days";
  PeriodUnitType["Weeks"] = "Weeks";
  PeriodUnitType["Years"] = "Years";
  PeriodUnitType["Do_Not_Delete"] = "Do Not Delete";
  PeriodUnitType["Do_Not_Retain"] = "Do Not Retain";
  return PeriodUnitType;
}({});
const usePeriods = (saveForm, version_ind, dataSourceType, source_type, retention_period_value, retention_period_type, reserve_retention_period_value, reserve_retention_period_type) => {
  const AuthService = getService('AuthService');
  const prevDataSourceType = Object(usehooks["c" /* usePrevious */])(dataSourceType);
  const previousSource_type = Object(usehooks["c" /* usePrevious */])(source_type);
  const [periodsData, setPeriodsData] = Object(react["useState"])(null);
  const updatePeriods = Object(react["useCallback"])(function () {
    let init = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : false;
    if (!periodsData) {
      return;
    }
    const updateData = {};
    const role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
    let periodTypes = [...(periodsData === null || periodsData === void 0 ? void 0 : periodsData.retentionPeriodTypes)] || false;
    let reservationPeriodTypes = [...(periodsData === null || periodsData === void 0 ? void 0 : periodsData.reservationPeriodTypes)] || false;
    let maxRetentionPeriod = periodsData.maxRetentionPeriod;
    let maxReservationPeriod = periodsData.maxReservationPeriod;
    let retentionDefaultPeriod = periodsData.retentionDefaultPeriod;
    const reservationDefaultPeriod = periodsData.reservationDefaultPeriod;
    const versioningPeriod = isTester => {
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
      periodTypes = periodTypes.filter(period => period.units <= maxRetentionPeriod.value);
    }
    if (maxReservationPeriod && maxReservationPeriod.value) {
      reservationPeriodTypes = reservationPeriodTypes.filter(period => period.units <= maxReservationPeriod.value);
    }
    updateData.periodTypes = periodTypes;
    updateData.reservationPeriodTypes = reservationPeriodTypes;
    updateData.maxReservationPeriod = maxReservationPeriod;
    updateData.maxRetentionPeriod = maxRetentionPeriod;
    if (init || periodTypes.findIndex(it => it.name === retention_period_type) < 0) {
      if (retentionDefaultPeriod) {
        updateData.retention_period_type = retentionDefaultPeriod.units;
        updateData.retention_period_value = retentionDefaultPeriod.value;
      }
    }
    if (init || reservationPeriodTypes.findIndex(it => it.name === reserve_retention_period_type) < 0) {
      if (reservationDefaultPeriod) {
        updateData.reserve_retention_period_type = reservationDefaultPeriod.units;
        updateData.reserve_retention_period_value = reservationDefaultPeriod.value;
      }
    }
    saveForm(updateData);
  }, [saveForm, version_ind, periodsData, AuthService, reserve_retention_period_value, reserve_retention_period_type, retention_period_value, dataSourceType, source_type]);
  Object(react["useEffect"])(() => {
    updatePeriods();
  }, [periodsData]);
  Object(react["useEffect"])(() => {
    if (retention_period_type === 'reset') {
      updatePeriods(true);
    }
  }, [retention_period_type]);
  Object(react["useEffect"])(() => {
    async function fetchReservationPeriodsData() {
      const data = await apis_task.getRetentionPeriodsData();
      data.reservationPeriodTypes.forEach(option => {
        option.label = option.name;
        option.value = option.units;
      });
      data.retentionPeriodTypes.forEach(option => {
        option.label = option.name;
        option.value = option.units;
      });
      setPeriodsData(data);
    }
    fetchReservationPeriodsData();
  }, []);
  Object(react["useEffect"])(() => {
    updatePeriods(false);
  }, [version_ind]);
  Object(react["useEffect"])(() => {
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
  Object(react["useEffect"])(() => {
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
/* harmony default export */ var Main_usePeriods = (usePeriods);
// CONCATENATED MODULE: ./src/components/Periods/index.tsx








const retentionMap = {
  periodTypes: 'retentionPeriodTypes',
  maxPeriod: 'maxRetentionPeriod',
  defaultPeriod: 'retentionDefaultPeriod',
  testersPeriods: 'retentionPeriodForTesters',
  versioning: 'versioningRetentionPeriod',
  period_type: 'retention_period_type',
  period_value: 'retention_period_value'
};
const reserveMap = {
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
  const {
    title,
    mandatory,
    period_type,
    onChange,
    period_value,
    periodsData,
    maxPeriod,
    disabled,
    reserve
  } = props;
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    register,
    errors
  } = Object(react["useContext"])(TaskContext);
  const [localOptions, setLocalOptions] = Object(react["useState"])(periodsData || []);
  const [periodFields, setPeriodFields] = Object(react["useState"])(retentionMap);
  const [selectedPeriodType, setSelectedPeriodType] = Object(react["useState"])();
  Object(react["useEffect"])(() => {
    if (reserve) {
      setPeriodFields(reserveMap);
    } else {
      setPeriodFields(retentionMap);
    }
  }, [reserve]);
  const [maxPeriodLocal, setMaxPeriodLocal] = Object(react["useState"])(Infinity);
  Object(react["useEffect"])(() => {
    if (selectedPeriodType && maxPeriod) {
      setMaxPeriodLocal(maxPeriod.value / selectedPeriodType.units);
    }
  }, [selectedPeriodType, maxPeriod]);
  Object(react["useEffect"])(() => {
    if (period_type) {
      const found = localOptions.find(it => it.name === period_type);
      if (found) {
        setSelectedPeriodType(found);
      }
    }
  }, [localOptions, period_type]);
  console.log(period_type);
  Object(react["useEffect"])(() => {
    if (periodsData) {
      setLocalOptions(periodsData);
    }
  }, [periodsData]);
  const periodTypeChange = Object(react["useCallback"])(option => {
    if (option.name === PeriodUnitType.Do_Not_Delete || option.name === PeriodUnitType.Do_Not_Retain) {
      onChange({
        [periodFields.period_type]: option.name,
        [periodFields.period_value]: option.name === PeriodUnitType.Do_Not_Delete ? -1 : 0
      });
    } else {
      onChange({
        [periodFields.period_type]: option.name,
        [periodFields.period_value]: 1
      });
    }
  }, [onChange, periodFields]);
  const periodValueChange = Object(react["useCallback"])(value => {
    onChange({
      [periodFields.period_value]: value
    });
  }, [onChange, periodFields]);
  const checkPeriodsValue = (value, taskData) => {
    const field_value = taskData[`${reserve ? 'reserve_retention' : 'retention'}_period_value`];
    const field_type = taskData[`${reserve ? 'reserve_retention' : 'retention'}_period_type`];
    if (field_value !== undefined) {
      if (field_value > maxPeriodLocal) {
        return `Selected retention period (${((selectedPeriodType === null || selectedPeriodType === void 0 ? void 0 : selectedPeriodType.units) || 0) * (period_value || 0)}) cannot exceed ${maxPeriod === null || maxPeriod === void 0 ? void 0 : maxPeriod.value} days`;
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
      error: (_errors = errors[`${reserve ? 'reserve_retenion' : 'retenion'}_period_value`]) === null || _errors === void 0 ? void 0 : _errors.message
    }), period_type !== 'Do Not Delete' && period_type !== 'Do Not Retain' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
      ...register(`${reserve ? 'reserve_retenion' : 'retenion'}_period_value`, {
        value: period_value,
        validate: {
          [`${reserve ? 'reserve_' : ''}retention_value`]: checkPeriodsValue
        }
      }),
      disabled: disabled,
      width: "60px",
      title: "",
      mandatory: mandatory,
      type: InputTypes.number,
      name: `${reserve ? 'reserve_retenion' : 'retenion'}_period_value`,
      value: period_value,
      min: (systemUserRole === null || systemUserRole === void 0 ? void 0 : systemUserRole.type) === 'admin' && reserve ? 0 : 1,
      max: maxPeriodLocal,
      onChange: periodValueChange
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var components_Periods = (Periods);
// CONCATENATED MODULE: ./src/containers/Task/Froms/TestDataStore/index.tsx







function TestDataStoreForm(props) {
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    version_ind,
    sync_mode,
    retention_period_type,
    retention_period_value,
    dataSourceType,
    maxRetentionPeriod,
    periodTypes,
    synthetic_type,
    source_type,
    userRole,
    sourceUserRole
  } = taskData;
  const onDataVersioningchange = Object(react["useCallback"])(value => {
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




const useWidgetStatus = (taskData, trigger, isValid, handleSubmit, failedComp) => {
  const [initTask, setInitTask] = Object(react["useState"])(true);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const [currentStep, setCurrentStep] = Object(react["useState"])('task_title');
  const [touchedForms, setTouchedForms] = Object(react["useState"])([]);
  const [submittedForm, setSubmittedForm] = Object(react["useState"])(true);
  const [statuses, setStatuses] = Object(react["useState"])({
    dataSourceStatus: StatusEnum.disabled,
    subsetStatus: StatusEnum.disabled,
    subsetPosition: SubsetPossition.undefined,
    testDataStoreStatus: StatusEnum.disabled,
    targetStatus: StatusEnum.disabled,
    isTargetConnectionEnabled: false,
    isSourceConnectionEnabled: false
  });

  // useEffect(() => {
  //     console.log(statuses);
  // }, [statuses]);

  const statusesMap = Object(react["useMemo"])(() => ({
    source: 'dataSourceStatus',
    source_data_subset: 'sourceSubsetStatus',
    target_data_subset: 'targetSubsetStatus',
    test_data_store: 'testDataStoreStatus',
    target: 'targetStatus'
  }), []);
  const getSourceStatus = taskData => {
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
  const getTestDataStoreStatus = taskData => {
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
  const getTargetStatus = (taskData, statuses, currentStep) => {
    const {
      sync_mode,
      version_ind,
      dataSourceType,
      source_type
    } = taskData;
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
  const subsetStatus = taskData => {
    const sourceStatus = getSourceStatus(taskData);
    const targetStatus = getTargetStatus(taskData, null, '');
    if (failedComp.indexOf('subset') >= 0) {
      return StatusEnum.blink;
    }
    const checkVersioningStatus = () => {
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
      } else if ((taskData.selection_method === 'P' || taskData.selection_method === 'PR') && taskData.selection_param_value && (taskData.maxToCopy == 9007199254740992 || taskData.num_of_entities || taskData.clone_ind)) {
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
  const subsetPosition = taskData => {
    if (taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'new_data') {
      return SubsetPossition.undefined;
    }
    if (taskData.dataSourceType === 'data_source' && taskData.sync_mode === 'OFF' || taskData.dataSourceType !== 'data_source' && taskData.synthetic_type === 'generated_data' || taskData.reserve_ind && !taskData.load_entity || taskData.delete_before_load && !taskData.load_entity) {
      return SubsetPossition.target;
    }
    return SubsetPossition.source;
  };
  const getSourceSubsetStatus = taskData => {
    if (subsetPosition(taskData) === SubsetPossition.source) {
      return subsetStatus(taskData);
    }
    return StatusEnum.disabled;
  };
  const getTargetSubsetStatus = taskData => {
    if (subsetPosition(taskData) === SubsetPossition.target) {
      return subsetStatus(taskData);
    }
    return StatusEnum.disabled;
  };
  const isTargetConnectionEnabled = taskData => {
    const targetStatus = getTargetStatus(taskData, null, '');
    const subsetStatusValue = subsetStatus(taskData);
    const subsetPositionValue = subsetPosition(taskData);
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
  const isSourceConnectionEnabled = taskData => {
    const sourceStatus = getSourceStatus(taskData);
    const subsetStatusValue = subsetStatus(taskData);
    const subsetPositionValue = subsetPosition(taskData);
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
  const statusesFuncMap = Object(react["useMemo"])(() => ({
    dataSourceStatus: getSourceStatus,
    sourceSubsetStatus: getSourceSubsetStatus,
    targetSubsetStatus: getTargetSubsetStatus,
    testDataStoreStatus: getTestDataStoreStatus,
    targetStatus: getTargetStatus,
    isTargetConnectionEnabled: isTargetConnectionEnabled,
    isSourceConnectionEnabled: isSourceConnectionEnabled,
    subsetStatus: subsetStatus,
    subsetPosition: subsetPosition
  }), [currentStep, failedComp]);
  const updateWidgetStatuses = Object(react["useCallback"])((taskData, currentStep) => {
    const statusesTemp = {
      ...statuses
    };
    Object.keys(statusesFuncMap).forEach(statusName => {
      statusesTemp[statusName] = statusesFuncMap[statusName](taskData, statusesTemp);
    });
    statusesTemp.targetStatus = statusesFuncMap.targetStatus(taskData, statusesTemp, currentStep);
    setStatuses(statusesTemp);
  }, [statuses, statusesFuncMap, touchedForms]);
  const [pendingStep, setPendingStep] = Object(react["useState"])('');
  const moveToStepAfterValidaity = Object(react["useCallback"])(() => {
    let stepTemp = '';
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
  const formOnError = Object(react["useCallback"])(data => {
    Object.keys(data).forEach(key => {
      if (!data[key].ref.step) {
        data[key].ref.step = currentStep;
      }
    });
    console.error(data);
    moveToStepAfterValidaity();
    return;
    if (stepsConfig[currentStep].mandatoryFields && stepsConfig[currentStep].mandatoryFields.length > 0) {
      const fields = Object.keys(data);
      const filteredArray = fields.filter(value => stepsConfig[currentStep].mandatoryFields.includes(value));
      if (filteredArray.length === 0) {
        moveToStepAfterValidaity();
        return;
      }
    }
    console.error(data);
    setPendingStep('');
  }, [pendingStep, currentStep]);
  const checkFormMandatory = Object(react["useCallback"])(() => {
    return stepsConfig[currentStep] && stepsConfig[currentStep].mandatory;
  }, [currentStep]);
  Object(react["useEffect"])(() => {
    if (pendingStep) {
      if (checkFormMandatory()) {
        handleSubmit(moveToStepAfterValidaity, formOnError)();
      } else {
        moveToStepAfterValidaity();
      }
    }
  }, [pendingStep, handleSubmit, moveToStepAfterValidaity, formOnError, checkFormMandatory]);
  const onClickStep = Object(react["useCallback"])(step => {
    setInitTask(false);
    if (!currentStep) {
      setCurrentStep(step);
      return;
    }
    if (currentStep && touchedForms.indexOf(currentStep) < 0) {
      setTouchedForms([...touchedForms, currentStep]);
    }
    if (step !== currentStep) {
      setPendingStep(step);
    }
  }, [currentStep, touchedForms, setInitTask]);
  Object(react["useEffect"])(() => {
    updateWidgetStatuses(taskData, currentStep);
  }, [taskData, currentStep]);
  return {
    statuses,
    onClickStep,
    currentStep,
    touchedForms,
    setTouchedForms,
    submittedForm,
    setSubmittedForm,
    statusesFuncMap,
    initTask
  };
};
/* harmony default export */ var Main_useWidgetStatus = (useWidgetStatus);
// CONCATENATED MODULE: ./src/components/RadioGroup/styles.ts

const RadioGroup_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding-bottom: 30px;
    width: 100%;
`;
const RadiosContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: ${props => props.direction};
    gap: 20px;
`;
const RadioGroup_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: normal;
    letter-spacing: -0.32px;
    text-align: center;
    color: #2e2e2e;
`;
const styles_RadioInput = styled_components_browser_esm["b" /* default */].input`
    margin-right: 10px;
    width: 20px;
    height: 20px;
`;
const RadioGroup_styles_Icon = styled_components_browser_esm["b" /* default */].img`

`;
// CONCATENATED MODULE: ./src/components/RadioGroup/index.tsx




function RadioGroup(props) {
  const {
    onChange,
    data,
    selectedValue,
    name,
    direction,
    title
  } = props;
  const getRadios = Object(react["useCallback"])(() => {
    return data.map(radioData => /*#__PURE__*/Object(jsx_runtime["jsx"])(components_radio, {
      onChange: onChange,
      name: name,
      value: radioData.value,
      selectedValue: selectedValue,
      title: radioData.title,
      disabled: radioData.disabled
    }));
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

const Target_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: center;
`;
const Target_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
`;
const EnvironmentContainer = styled_components_browser_esm["b" /* default */].div`
    display: ${props => props.hide ? 'none' : 'block'};
    margin-top: 12px;
`;
const TaskActionContainer = styled_components_browser_esm["b" /* default */].div`
    margin-top: 10px;
`;
const Target_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 15px;
    flex-direction: column;
    margin-top: 15px;
`;
const Target_styles_DataMovmentSettingsContainer = styled_components_browser_esm["b" /* default */].div`
    margin-top: 15px;
`;
const Target_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.25;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    padding-bottom: 15px;
    border-bottom: ${props => props.widthBorder ? '1px solid #ccc' : ''};
`;
const Section = styled_components_browser_esm["b" /* default */].div`
    margin-top: 30px;
    display: flex;
    flex-direction: row;

`;
const SectionItemConatiner = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 30px;
    flex-direction: column;
    width: 100%;
    padding: 0px 21px;
`;
const ActionContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 30px;

`;
const EntityCloneContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    width: 100%;
    gap: 15px;
    height: 30px;
    align-items: center;
`;
const Target_styles_MadatoryAsterisk = styled_components_browser_esm["b" /* default */].span`
    color: red;
`;
const CheckBoxContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100px;
`;
const SectionTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
`;
const RadioGroupContainer = styled_components_browser_esm["b" /* default */].div`
    border-bottom: 1px solid #ccc;
`;
const TestingEnvironmentContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    margin-top: 13px;
`;
const styles_Leftside = styled_components_browser_esm["b" /* default */].div`
    width: 320px;
    display: flex;
    flex-direction: column;
    gap: 20px;

`;
const Target_styles_RightSide = styled_components_browser_esm["b" /* default */].div`
    border-left: 2px solid #ccc;
    padding-left: 35px;
    flex-grow: 1;
`;
const EntityCloneComment = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #666;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/Target/index.tsx













function TargetForm(props) {
  var _errors$num_of_entiti;
  const {
    register,
    clearErrors,
    errors,
    taskData,
    saveForm,
    unregister
  } = Object(react["useContext"])(TaskContext);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    be_name,
    environment_id,
    load_entity,
    delete_before_load,
    reserve_ind,
    replace_sequences,
    reserve_retention_period_type,
    reserve_retention_period_value,
    reserve_note,
    clone_ind,
    num_of_clones,
    target_env,
    reservationPeriodTypes,
    maxReservationPeriod,
    be_id,
    dataSourceType,
    selection_method,
    version_ind,
    source_type,
    synthetic_type,
    generation_type,
    maxToCopy,
    userRole,
    deleteWarning,
    source_environment_id,
    reserve_only_task
  } = taskData;
  const toast = hooks_useToast();
  const [disableAI, setDisableAI] = Object(react["useState"])(false);
  Object(react["useEffect"])(() => {
    async function fetchData() {
      try {
        const data = await apis_task.getEnvironments(undefined, true);
        const ai_env = data.find(it => it.synthetic_indicator === 'AI');
        if (!ai_env || ai_env.permission.indexOf('write') < 0) {
          setDisableAI(true);
        }
      } catch (err) {
        setDisableAI(true);
      }
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
  Object(react["useEffect"])(() => {
    if (dataSourceType === 'ai_generated') {
      setDisableAI(true);
    }
  }, [dataSourceType]);
  Object(react["useEffect"])(() => {
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
  const targetEnvironmentsTypes = Object(react["useMemo"])(() => {
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
  Object(react["useEffect"])(() => {
    const updateData = {};
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
  const targetEnvChange = Object(react["useCallback"])(item => {
    const updatedData = {
      environment_id: item && item.environment_id || undefined,
      environment_name: item && item.environment_name || undefined
    };
    if (!source_environment_id) {
      updatedData.mask_sensitive_data = item && item.mask_sensitive_data || false;
    }
    saveForm(updatedData);
  }, [saveForm]);
  Object(react["useEffect"])(() => {
    if (dataSourceType === 'ai_generated' || dataSourceType === 'synthetic') {
      saveForm({
        replace_sequences: true
      });
    }
  }, [dataSourceType]);
  Object(react["useEffect"])(() => {
    if (selection_method === 'ALL') {
      saveForm({
        clone_ind: false
      });
    }
  }, [selection_method]);
  Object(react["useEffect"])(() => {
    if (version_ind) {
      const updateData = {
        load_entity: true
      };
      if (deleteWarning === undefined) {
        updateData.delete_before_load = true;
      }
      saveForm(updateData);
    }
  }, [version_ind]);
  const actionChange = Object(react["useCallback"])((action, value) => {
    saveForm({
      [action]: value
    });
  }, [saveForm]);
  const replaceSequenceChange = Object(react["useCallback"])(value => {
    saveForm({
      replace_sequences: value || false
    });
  }, [saveForm]);
  const entityCloneChange = Object(react["useCallback"])(value => {
    saveForm({
      clone_ind: value || false,
      num_of_clones: undefined
    });
    clearErrors('num_of_clones');
  }, [saveForm, clearErrors]);
  const reserveNoteChange = Object(react["useCallback"])(value => {
    saveForm({
      reserve_note: value
    });
  }, [saveForm]);
  const numberOfCloneChange = Object(react["useCallback"])(value => {
    saveForm({
      num_of_clones: value
    });
  }, [saveForm]);
  const targetEnvTypeChange = Object(react["useCallback"])(value => {
    async function fetchCheckAIInstaltion() {
      try {
        await apis_task.checkAIInstallation('AITraining');
      } catch (err) {}
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
  Object(react["useEffect"])(() => {
    if (!target_env) {
      saveForm({
        target_env: 'target_env'
      });
    }
  }, []);
  Object(react["useEffect"])(() => {
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
                  onChange: value => actionChange('delete_before_load', value || false),
                  name: "delete_checkbox",
                  value: delete_before_load
                }), dataSourceType === 'data_source' && source_type === 'tables' ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {
                  children: "Delete the entire table data"
                }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
              }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionContainer, {
                children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(CheckBoxContainer, {
                  children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
                    disabled: reserve_only_task || delete_before_load && reserve_ind || dataSourceType === 'data_source' && source_type === 'tables' || version_ind && !(dataSourceType === 'data_source' && source_type === 'tables'),
                    title: "Load",
                    onChange: value => actionChange('load_entity', value || false),
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
                      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
                        ...register('num_of_clones', {
                          required: 'Populate number of clones',
                          min: {
                            value: 1,
                            message: 'Minimum Entites to clone is 1'
                          },
                          max: {
                            value: maxToCopy,
                            message: `Maximum Entites to clone is ${maxToCopy}`
                          }
                        }),
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
                      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(EntityCloneComment, {
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
                    onChange: value => actionChange('reserve_ind', value || false),
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

const Advanced_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: center;
`;
const Advanced_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    position: relative;
`;
const styles_Section = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap : 27px;
    padding-bottom: 30px;
    border-bottom: solid 1px #ccc;
`;
const TableWrapper = styled_components_browser_esm["b" /* default */].div`
    margin-top: 30px;
`;
const ActionsColumn = styled_components_browser_esm["b" /* default */].div`
    display:flex;
    align-items: center;
    gap: 7px;
`;
const ButtonContainer = styled_components_browser_esm["b" /* default */].div`
    padding-bottom: 9px;
    width: 100%;
    display: flex;
    justify-content: flex-end;
`;
const Advanced_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const styles_ResetButton = styled_components_browser_esm["b" /* default */].div`
    z-index: 1;
    position: absolute;
    right: 0px;
    top: -50px;
    z-index: 100;
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1683f2;
    display: flex;
    gap: 6px;
    align-items: center;
    cursor: pointer;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/Scheduler/styles.ts

const Scheduler_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: center;
`;
const Scheduler_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 15px;
`;
const SchedulerTypes = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
    padding-bottom: 30px;
    border-bottom: solid 1px #ccc;
`;
const EndByDate = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    align-items: center;
    gap: 20px;
    padding-bottom: 30px;
    border-bottom: solid 1px #ccc;  
`;
const CronContainer = styled_components_browser_esm["b" /* default */].div`
`;
const EndByContainer = styled_components_browser_esm["b" /* default */].div`
    padding-top: 30px;
`;
const styles_DateContainer = styled_components_browser_esm["b" /* default */].div`
    padding-top: 30px;
`;
const TimeNote = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    justify-content: flex-end;
    color: #ec4758;
    font-size: 14px;
`;
// EXTERNAL MODULE: ./node_modules/angular/index.js
var node_modules_angular = __webpack_require__(63);
var angular_default = /*#__PURE__*/__webpack_require__.n(node_modules_angular);

// CONCATENATED MODULE: ./src/components/AngularJSWrapper/index.tsx



function AngularJSWrapper(props) {
  const {
    comp,
    params,
    save
  } = props;
  const containerRef = Object(react["useRef"])(null);
  const [scope, setScope] = Object(react["useState"])({});
  Object(react["useEffect"])(() => {
    Object.keys(params).forEach(it => {
      scope[params[it].name] = params[it].value;
    });
  }, [params, scope]);
  Object(react["useEffect"])(() => {
    if (containerRef.current) {
      // Manually bootstrap the AngularJS application

      // Compile and inject the AngularJS component
      const $injector = angular_default.a.element(containerRef.current).injector();
      const $compile = $injector.get('$compile');
      const $rootScope = $injector.get('$rootScope');
      const newScope = $rootScope.$new();
      Object.keys(params).forEach(key => {
        newScope[params[key].name] = params[key].value;
      });
      const element = angular_default.a.element(`<${comp} ${getParams()}></${comp}>`);
      const compiledElement = $compile(element)(newScope);
      containerRef.current.appendChild(compiledElement[0]);
      newScope.$apply();
      setScope(newScope);
      newScope.$watch(params['ng-model'].name, (newValue, value) => {
        save(params['ng-model'].name, newValue);
      }, true, true);
    }
    // Cleanup on component unmount
    return () => {
      // Remove the AngularJS component and clean up the scope
      if (containerRef.current) {
        const scope = angular_default.a.element(containerRef.current).scope();
        scope.$destroy();
      }
    };
  }, []);
  const getParams = Object(react["useCallback"])(() => {
    const attributes = Object.keys(params).map(it => ` ${it}="${params[it].name}" `).join(' ');
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
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const {
    scheduler,
    scheduling_end_date
  } = taskData;
  const [cronValue, setCronValue] = Object(react["useState"])(scheduler || '');
  const [cronEndDate, setCornEndDate] = Object(react["useState"])(scheduling_end_date && new Date(scheduling_end_date) || null);
  const [schedulingParameters, setSchedulingParameters] = Object(react["useState"])(false);
  const [endBy, setEndBy] = Object(react["useState"])(scheduling_end_date ? 'end_by' : 'none');
  Object(react["useEffect"])(() => {
    setSchedulingParameters(scheduler !== 'immediate');
  }, [scheduler]);
  const schedulingParametersOnChange = Object(react["useCallback"])(value => {
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
  const cronChange = Object(react["useCallback"])(value => {
    setCronValue(value);
    saveForm({
      scheduler: value
    });
  }, [saveForm]);
  const endByChange = Object(react["useCallback"])(value => {
    setEndBy(value);
    saveForm({
      scheduling_end_date: value === 'none' ? null : cronEndDate
    });
  }, [saveForm, cronEndDate]);
  const cronEndDateChange = Object(react["useCallback"])(value => {
    setCornEndDate(value);
    saveForm({
      scheduling_end_date: value && value.toDateString() || null
    });
  }, [saveForm]);
  const translateFn = key => {
    if (key === 'hour') {
      return 'hour(s)';
    }
    return key;
  };
  const params = Object(react["useMemo"])(() => ({
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
  }), [scheduler]);
  const saveSchedulerValue = Object(react["useCallback"])((field, value) => {
    saveForm({
      [field]: value
    });
  }, [saveForm]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Scheduler_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(Scheduler_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
        name: 'set_scheduling_parameters',
        title: 'Set scheduling parameters',
        onChange: value => schedulingParametersOnChange(value),
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
// CONCATENATED MODULE: ./src/images/delete-icon-blue.svg
/* harmony default export */ var delete_icon_blue = ("js/dist/437f49031698b544e4b1c9c124aaf20a.svg");
// CONCATENATED MODULE: ./src/images/edit.svg
/* harmony default export */ var edit = ("js/dist/ad51b02c0cfd4e1a26763e5d4e87b925.svg");
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariables/useTable.tsx






const TaskVariables_useTable_useTable = (deleteGlobal, editGloabl) => {
  const columnHelper = Object(lib_index_esm["a" /* createColumnHelper */])();
  const columns = Object(react["useMemo"])(() => [{
    id: 'actions',
    header: '',
    cell: _ref => {
      let {
        row
      } = _ref;
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ActionsColumn, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Icon, {
          onClick: () => editGloabl(row.original.global_name),
          src: edit
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_styles_Icon, {
          onClick: () => deleteGlobal(row.original.global_name),
          src: delete_icon_blue
        })]
      });
    }
  }, columnHelper.accessor('global_name', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Variable name"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('global_value', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Variable value"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  }), columnHelper.accessor('lu_name', {
    header: () => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: "Logical unit"
    }),
    cell: info => /*#__PURE__*/Object(jsx_runtime["jsx"])("span", {
      children: info.getValue()
    })
  })], [editGloabl, deleteGlobal, columnHelper]);
  return {
    columns
  };
};
/* harmony default export */ var TaskVariables_useTable = (TaskVariables_useTable_useTable);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariables/styles.ts

const styles_ButtonContainer = styled_components_browser_esm["b" /* default */].div`
    padding-bottom: 9px;
    position: relative;
    display: flex;
    justify-content: flex-end;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariablesModal/styles.ts

const TaskVariablesModal_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 324px;
    position: relative;
    z-index: 100;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 19px 0px 30px 0px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    background-color: #fff;
`;
const TaskVariablesModal_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 18px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    position: relative;
    margin: 0px 20px;
    margin-bottom: 19px;
`;
const TaskVariablesModal_styles_Body = styled_components_browser_esm["b" /* default */].div`
    margin: 24px 25px 0px 30px;
`;
const ItemsContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;

`;
const TaskVariablesModal_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border: solid 1px #ccc;
`;
const styles_CloseIcon = styled_components_browser_esm["b" /* default */].img`
    position: absolute;
    right: 0px;
    top: 5px;
    cursor: pointer;
`;
const TaskVariablesModal_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const TaskVariablesModal_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const TaskVariablesModal_styles_ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const styles_ErrorContainer = styled_components_browser_esm["b" /* default */].div`
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/TaskVariablesModal/index.tsx








function TaskVariablesModal(props) {
  const {
    setOpen,
    variableData,
    selectedVariables,
    lus,
    addGlobal
  } = props;
  const [chosenVariable, setChosenVariable] = Object(react["useState"])();
  const [localVariablesData, setLocalVariablesData] = Object(react["useState"])();
  const [chosenLu, setChosenLu] = Object(react["useState"])(null);
  const [variableLUList, setVariableLUList] = Object(react["useState"])([]);
  const [showError, setShowError] = Object(react["useState"])(false);
  const [variableValue, setVariableValue] = Object(react["useState"])();
  Object(react["useEffect"])(() => {
    async function fetchTaskVariables() {
      try {
        const data = await apis_task.getGlobalVariables(lus);
        const globals = [];
        data.forEach(global => {
          const luObjectMapping = {};
          const selectedLuForGlobal = selectedVariables.filter(it => it.global_name === global.globalName);
          if (selectedLuForGlobal.find(it => it.lu_name === 'ALL') && (!variableData || variableData.global_name !== global.globalName)) {
            return;
          }
          global.luList.forEach(luData => {
            luObjectMapping[luData.luName] = luData.defaultValue;
          });
          if (luObjectMapping['ALL'] !== undefined && luObjectMapping['ALL'] !== null) {
            const newLuList = [];
            if (selectedLuForGlobal.length > 0) {}
            if (selectedLuForGlobal.length === 0) {
              newLuList.push({
                luName: 'ALL',
                defaultValue: luObjectMapping['ALL'],
                value: 'ALL',
                label: 'ALL'
              });
            }
            lus.forEach(lu => {
              newLuList.push({
                luName: lu,
                defaultValue: luObjectMapping[lu] || luObjectMapping['ALL'] || '',
                value: lu,
                label: lu
              });
            });
            global.luList = newLuList;
          }
          global.luList = global.luList.filter(lu => selectedLuForGlobal.findIndex(it => it.lu_name === lu.luName) < 0 || lu.luName === (variableData === null || variableData === void 0 ? void 0 : variableData.lu_name));
          if (global.luList.length === 0 && (!variableData || variableData.global_name !== global.globalName)) {
            return;
          }
          if (variableData && !global.luList.find(it => it.luName === (variableData === null || variableData === void 0 ? void 0 : variableData.lu_name))) {
            global.luList.push({
              luName: variableData.lu_name,
              defaultValue: variableData.global_value,
              value: variableData.lu_name,
              label: variableData.lu_name
            });
          }
          globals.push({
            ...global,
            value: global.globalName,
            label: global.globalName
          });
        });
        if (variableData) {
          const found = globals.find(it => it.globalName === variableData.global_name);
          if (found) {
            setChosenVariable(found);
            setVariableLUList(found.luList.map(it => ({
              ...it,
              value: it.luName,
              label: it.luName
            })));
            const foundLU = found.luList.find(it => it.luName === variableData.lu_name);
            if (foundLU) {
              setChosenLu(foundLU);
              setVariableValue(variableData.global_value);
            }
          }
        }
        setLocalVariablesData(globals);
      } catch (err) {}
    }
    fetchTaskVariables();
  }, []);
  console.log(localVariablesData);
  const variableChange = Object(react["useCallback"])(value => {
    setShowError(false);
    setChosenVariable(value);
    setChosenLu(null);
    setVariableValue('');
    setVariableLUList(value.luList.map(it => ({
      ...it,
      value: it.luName,
      label: it.luName
    })));
  }, [setChosenVariable, setVariableLUList, setChosenLu, setVariableValue, setShowError]);
  const luChange = Object(react["useCallback"])(value => {
    setShowError(false);
    setChosenLu(value);
    if (value.defaultValue) {
      setVariableValue(value.defaultValue);
    } else {
      setVariableValue('');
    }
  }, [setChosenLu, setVariableValue, setShowError]);
  const varaiableValueChange = Object(react["useCallback"])(value => {
    setShowError(false);
    setVariableValue(value);
  }, [setVariableValue, setShowError]);
  const saveGlobal = Object(react["useCallback"])(() => {
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
        onClick: () => setOpen(false),
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
          onClick: () => setOpen(false),
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









function TaskVariables(props) {
  const {} = props;
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const [editVariableData, setEditVariableData] = Object(react["useState"])(undefined);
  const [open, setOpen] = Object(react["useState"])(false);
  const ref = Object(react["useRef"])();
  const {
    task_id,
    selected_logical_units_names,
    globals
  } = taskData;
  const editGloabl = Object(react["useCallback"])(globalName => {
    console.log(globals);
    const found = globals.find(it => it.global_name === globalName);
    if (found) {
      setEditVariableData({
        ...found
      });
      setOpen(true);
    }
  }, [globals, setEditVariableData]);
  console.log(globals);
  const deleteGlobal = Object(react["useCallback"])(globalName => {
    const newGlobals = globals.filter(it => it.global_name !== globalName);
    saveForm({
      globals: newGlobals
    });
  }, [saveForm, globals]);
  const {
    columns
  } = TaskVariables_useTable(deleteGlobal, editGloabl);
  const addNewGloabl = Object(react["useCallback"])(data => {
    if (data.edit) {
      const foundGlobal = globals.find(it => it.global_name === data.global_name);
      if (foundGlobal) {
        foundGlobal.lu_name = data.lu_name;
        foundGlobal.global_value = data.global_value;
      }
      saveForm({
        globals: [...globals]
      });
      setEditVariableData(undefined);
      return;
    }
    const newGlobals = [...globals, data];
    saveForm({
      globals: [...newGlobals]
    });
  }, [globals, saveForm]);
  const getTaskVariablesModal = Object(react["useCallback"])(() => {
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
          onClick: () => setOpen(true),
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
            width: "152px",
            type: 'secondary',
            title: "Set task variables",
            onClick: () => {}
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

const ExecutionPorcesses_styles_Container = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    gap: 15px;
`;
const AddButtonContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    justify-content: flex-end;
`;
const ExecutionPorcesses_styles_TableContainer = styled_components_browser_esm["b" /* default */].table`
    border-spacing: 0;
    border-collapse: separate;
    width: 100%;
    height: 100%;
`;
const ExecutionPorcesses_styles_Thead = styled_components_browser_esm["b" /* default */].thead`
    display: table;
    width: calc(100% - 5px);
    table-layout: fixed;
`;
const TheadRow = styled_components_browser_esm["b" /* default */].tr`
    vertical-align: top;
`;
const TheadEmptyColumn = styled_components_browser_esm["b" /* default */].th`
    text-align: start;
    font-size: 14px;
    padding: 4px 6px;
    vertical-align: middle;
    width: 45px;
    background-color: white;
    border: 0;
    color: black;
`;
const TheadNameColumn = styled_components_browser_esm["b" /* default */].th`
    overflow: visible;
    white-space: nowrap;
    text-align: start;
    font-size: 14px;
    padding: 4px 6px;
    vertical-align: middle;
    background-color: white;
    border: 0;
    color: black;
`;
const TheadOrderColumn = styled_components_browser_esm["b" /* default */].th`
    width: 20%;
    padding-left: 8px;
    text-align: center;
    text-align: start;
    font-size: 14px;
    padding: 4px 6px;
    vertical-align: middle;
    background-color: white;
    border: 0;
    color: black;
`;
const TheadDeleteColumn = styled_components_browser_esm["b" /* default */].th`
    width: 40px;
    text-align: center;
    text-align: start;
    font-size: 14px;
    padding: 4px 6px;
    vertical-align: middle;
    background-color: white;
    border: 0;
`;
const TBody = styled_components_browser_esm["b" /* default */].tbody`
    display: block;
    overflow-x: hidden;
    border-spacing: 0;
    overflow: auto;
`;
const TBodyRow = styled_components_browser_esm["b" /* default */].tr`
    display: table;
    width: calc(100% - 5px);
    min-height: 42px;
    table-layout: fixed;
    vertical-align: top;
    margin: 6px 0;
    margin-bottom: ${props => props.expand ? '0px' : ''};
    &:hover {
        background-color: transparent;
    }
`;
const TBodyEditColumn = styled_components_browser_esm["b" /* default */].td`
    border: unset;
    padding: unset;
    white-space: unset;
    max-width: unset;
    overflow: unset;

    text-align: start;
    padding: 4px 6px;
    vertical-align: middle;
    width: 45px;
    border: 1px solid #cccccc;
    border-left: none;
    border: none;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc;
    border-top-left-radius: 8px;
    border-bottom-left-radius: ${props => props.expand ? '0px' : '8px'};
    border-left: 1px solid #cccccc;
`;
const TBodyNameColumn = styled_components_browser_esm["b" /* default */].td`
    text-align: start;
    padding: 4px 6px;
    vertical-align: middle;
    border: 1px solid #cccccc;
    border-left: none;
    border: none;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc;
    color: ${props => props.expand ? 'rgb(20, 131, 243);' : ''};
`;
const TBodyOrderColumn = styled_components_browser_esm["b" /* default */].td`
    text-align: start;
    padding: 4px 6px;
    vertical-align: middle;
    border: 1px solid #cccccc;
    border-left: none;
    border: none;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc;
    width: 20%;
    max-width: 20%;
    overflow: visible;
    white-space: nowrap;
`;
const TBodyDeleteColumn = styled_components_browser_esm["b" /* default */].td`
    text-align: start;
    padding: 4px 6px;
    vertical-align: middle;
    border: 1px solid #cccccc;
    border-left: none;
    border: none;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc;
    width: 40px;
    text-align: center;
    border-top-right-radius: 8px;
    border-bottom-right-radius: ${props => props.expand ? '0px' : '8px'};
    border-right: 1px solid #cccccc;
`;
const ExecutionPorcesses_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const EditIconContainer = styled_components_browser_esm["b" /* default */].div`
    border: 1px solid  ${props => props.expand ? '#0c84f3' : '#2e2e2e'};
    background: ${props => props.expand ? '#0c84f3' : ''};
    color: ${props => props.expand ? '#fff' : '#2e2e2e'};
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 22px;
    height: 22px;
    padding: 2px;
    font-size: 16px;
    cursor: pointer;
    transition: all .5s ease;
    margin: 5px;
    user-select: none;
`;
const EditIcon = styled_components_browser_esm["b" /* default */].div`
    padding: 0px 0px 0px 0px;
    user-select: none;
    color: ${props => props.mandatory ? 'red' : props.edited ? '#1683f2' : ''};
`;
const TBodyExpandRow = styled_components_browser_esm["b" /* default */].tr`
    padding-top: 0 !important;
    border: 1px solid #cccccc !important;
    border-top: none !important;
    width: calc(100% - 5px) !important;
    margin-bottom: 6px;
    border-bottom-left-radius: 8px;
    border-bottom-right-radius: 8px;
    vertical-align: top;
    margin: 0px 0px 6px 0px;
    display: table;
    table-layout: fixed;
    background-color: white !important;
`;
const TBodyExpandContainer = styled_components_browser_esm["b" /* default */].td`
    text-align: start;
    display: flex;
    border: 1px solid #cccccc;
    padding-bottom: 10px !important;
    border: none !important;
    padding: 4px 6px;
    vertical-align: middle;
    border: 1px solid #cccccc;
    border-left: none;
    border: none;
    border-top: 1px solid #ccc;
    border-bottom: 1px solid #ccc;
    max-width: 100%;
    width: 100%;
`;
const TBodyExpandContent = styled_components_browser_esm["b" /* default */].div`
    user-select: none;
    width: 50%;
    margin: 0 35px;
`;
const ModalAddContainer = styled_components_browser_esm["b" /* default */].div`
    width: 400px;
    min-height: 300px;
    position: relative;
    z-index: 100;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 19px 0px 30px 0px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    background-color: #fff;
`;
const ExecutionPorcesses_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 18px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    position: relative;
    margin: 0px 20px;
    margin-bottom: 19px;
`;
const ExecutionPorcesses_styles_Body = styled_components_browser_esm["b" /* default */].div`
    margin: 24px 25px 0px 30px;
`;
const ExecutionPorcesses_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border: solid 1px #ccc;
`;
const ExecutionPorcesses_styles_CloseIcon = styled_components_browser_esm["b" /* default */].img`
    position: absolute;
    right: 0px;
    top: 5px;
    cursor: pointer;
`;
const ExecutionPorcesses_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const ExecutionPorcesses_styles_ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/ExecutionPorcesses/index.tsx












function ExecutionPorcesses(props) {
  const {
    rows,
    processType,
    save,
    data
  } = props;
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const [options, setOptions] = Object(react["useState"])([]);
  const [open, setOpen] = Object(react["useState"])(false);
  const ref = Object(react["useRef"])();
  const [processesData, setProcessesData] = Object(react["useState"])([]);
  const [chosenProcesses, setChosenProcesses] = Object(react["useState"])([]);
  const [expandedRows, setExpandedRows] = Object(react["useState"])([]);
  Object(react["useEffect"])(() => {
    async function fetchExecutionProcessParam() {
      try {
        const params_data = await apis_task.getExecutionProcessParams(processType, rows.map(it => it.process_name));
        rows.map(it => {
          const found_params = params_data.find(it2 => it.process_name === it2.process_name);
          it.editors = (found_params === null || found_params === void 0 ? void 0 : found_params.editors) || [];
          it.editors = it.editors.map(it => {
            var _it$editor;
            it.name = (_it$editor = it.editor) === null || _it$editor === void 0 ? void 0 : _it$editor.name;
            if (it.default) {
              it.value = it.default;
              it.editor.value = it.value;
            }
            return it;
          });
        });
        (data || []).forEach(processData => {
          const foundRow = rows.find(it => it.process_name === processData.process_name);
          if (foundRow) {
            var _processData$paramete;
            processData.editors = foundRow.editors;
            ((processData === null || processData === void 0 ? void 0 : (_processData$paramete = processData.parameters) === null || _processData$paramete === void 0 ? void 0 : _processData$paramete.inputs) || []).forEach(param => {
              const foundEditor = processData.editors.find(it => it.name === param.name);
              if (foundEditor) {
                foundEditor.value = param.value || null;
                foundEditor.editor.value = param.value || null;
                foundEditor.editor.schema2 = param.schema || null;
              }
            });
          }
        });
        setProcessesData([...data]);
        setOptions(rows);
      } catch (err) {}
    }
    fetchExecutionProcessParam();
  }, [rows]);
  const expandRow = Object(react["useCallback"])(process_id => {
    setExpandedRows(oldArray => {
      if (oldArray.indexOf(process_id) >= 0) {
        return oldArray.filter(it => it !== process_id);
      } else {
        return [...oldArray, process_id];
      }
    });
  }, [setExpandedRows]);
  const updateFabricEditorValues = (processName, values) => {
    values.forEach(data => {
      updateParamsValue(processName, data.name, data.value, data.schema);
    });
  };
  const updateParamsValue = Object(react["useCallback"])((processName, name, value, schema) => {
    const processData = processesData.find(it => it.process_name === processName);
    if (!processData) {
      return;
    }
    const newParams = processData.editors;
    const index = newParams.findIndex(param => param.name === name);
    if (index >= 0) {
      newParams[index].value = value;
      newParams[index].schema = schema;
      newParams[index].editor.value = value;
      const parameters = {
        inputs: (newParams || []).map(it => {
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
      setProcessesData([...processesData]);
    }
  }, [processesData, save, processType]);
  const getProcessEditors = Object(react["useCallback"])(processName => {
    const processData = processesData.find(it => it.process_name === processName);
    if (!processData) {
      return [];
    }
    return processData.editors.map(it => {
      if (it.editor && it.editor.value === undefined) {
        it.editor.value = null;
      }
      return it.editor;
    });
  }, [processesData]);
  const closeModal = Object(react["useCallback"])(() => {
    setOpen(false);
    setChosenProcesses([]);
  }, [setOpen]);
  const addProcess = Object(react["useCallback"])(() => {
    if (chosenProcesses.length > 0) {
      setProcessesData(oldArray => {
        return [...oldArray, ...chosenProcesses];
      });
    }
    setOpen(false);
    setChosenProcesses([]);
  }, [setOpen, setProcessesData, setChosenProcesses, chosenProcesses]);
  const deleteProcess = Object(react["useCallback"])(processName => {
    setProcessesData(oldArray => {
      return oldArray.filter(it => it.process_name !== processName);
    });
  }, [setProcessesData]);
  const onProcessChange = Object(react["useCallback"])(value => {
    setChosenProcesses(value);
  }, [setOpen, setProcessesData, setChosenProcesses, chosenProcesses]);
  Object(react["useEffect"])(() => {
    save(processType, processesData);
  }, [processesData]);
  const getAddProcessContainer = Object(react["useCallback"])(() => {
    return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ModalAddContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Title, {
        children: [`${processType === 'pre' ? 'Pre' : 'Post'} execution process`, /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_CloseIcon, {
          onClick: () => closeModal(),
          src: xclose
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Body, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
          title: "",
          maxMenuHeight: 120,
          value: chosenProcesses,
          onChange: onProcessChange,
          options: (options || []).filter(it => {
            return processesData.findIndex(it2 => it.process_name === it2.process_name) < 0;
          }).map(it => ({
            ...it,
            label: it.process_name,
            value: it.process_id
          })),
          loading: false,
          isMulti: true,
          enableSelectAll: false
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionPorcesses_styles_Actions, {
          border: false,
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_ActionItem, {
            onClick: () => closeModal(),
            children: "Cancel"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionPorcesses_styles_ActionItem, {
            onClick: () => addProcess(),
            children: "Save"
          })]
        })]
      })]
    });
  }, [processType, options, setOpen, chosenProcesses, processesData]);
  const getMandatoryEditors = processData => {
    return processData.editors.findIndex(it => it.mandatory && (it.value === null || it.value === undefined)) >= 0;
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
          onClick: () => setOpen(true),
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
            title: 'Add Process',
            type: 'secondary',
            width: '150px',
            onClick: () => {},
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
        children: processesData.sort((a, b) => a.execution_order - b.execution_order).map(it => /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
          children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TBodyRow, {
            expand: expandedRows.indexOf(it.process_id) >= 0,
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyEditColumn, {
              expand: expandedRows.indexOf(it.process_id) >= 0,
              children: getProcessEditors(it.process_name).length === 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {}) : /*#__PURE__*/Object(jsx_runtime["jsx"])(EditIconContainer, {
                expand: expandedRows.indexOf(it.process_id) >= 0,
                onClick: () => expandRow(it.process_id),
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
                onClick: () => {
                  deleteProcess(it.process_name);
                },
                src: delete_icon_gray
              })
            })]
          }), expandedRows.indexOf(it.process_id) >= 0 ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandRow, {
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandContainer, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TBodyExpandContent, {
                children: /*#__PURE__*/Object(jsx_runtime["jsx"])(fabricWidget, {
                  updateValues: values => updateFabricEditorValues(it.process_name, values),
                  editor: getProcessEditors(it.process_name),
                  saveRef: data => {}
                }, `${processType}_execution_process`)
              })
            })
          }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
        }))
      })]
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var Advanced_ExecutionPorcesses = (ExecutionPorcesses);
// CONCATENATED MODULE: ./src/containers/Task/Froms/Advanced/index.tsx












function AdvancedForm(props) {
  const {
    be_id
  } = props;
  const {
    taskData,
    saveForm
  } = Object(react["useContext"])(TaskContext);
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const {
    postExecutionProcesses,
    preExecutionProcesses,
    scheduler,
    globals,
    sourceUserRole,
    userRole,
    enable_masking_only
  } = taskData;
  console.log('taskData', taskData);
  const [preLoading, setPreLoading] = Object(react["useState"])(true);
  const [postLoading, setPostLoading] = Object(react["useState"])(true);
  const [preExecutionProcessOptions, setPreExecutionProcessOptions] = Object(react["useState"])([]);
  const [postExecutionProcessOptions, setPostExecutionProcessOptions] = Object(react["useState"])([]);
  Object(react["useEffect"])(() => {
    async function fetchPreExecutionProcess() {
      try {
        if (!be_id) {
          return;
        }
        const data = await apis_task.getPreExecutionProcess(be_id);
        setPreExecutionProcessOptions(data);
        setPreLoading(false);
      } catch (err) {
        // use hook toast
        setPreLoading(false);
      }
    }
    async function fetchPostExecutionProcess() {
      try {
        if (!be_id) {
          return;
        }
        const data = await apis_task.getPostExecutionProcess(be_id);
        setPostExecutionProcessOptions(data);
        setPostLoading(false);
      } catch (err) {
        // use hook toast
        setPostLoading(false);
      }
    }
    fetchPreExecutionProcess();
    fetchPostExecutionProcess();
  }, []);
  const saveExecutionProcesses = Object(react["useCallback"])((processType, data) => {
    saveForm({
      [`${processType}ExecutionProcesses`]: data
    });
  }, [saveForm]);
  const getPreProcessBody = () => {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_ExecutionPorcesses, {
      rows: preExecutionProcessOptions,
      processType: 'pre',
      save: saveExecutionProcesses,
      data: preExecutionProcesses
    }, 'pre_execution_container');
  };
  const getPostProcessBody = Object(react["useCallback"])(() => {
    return /*#__PURE__*/Object(jsx_runtime["jsx"])(Advanced_ExecutionPorcesses, {
      rows: postExecutionProcessOptions,
      processType: 'post',
      save: saveExecutionProcesses,
      data: postExecutionProcesses
    }, 'post_execution_container');
  }, [postExecutionProcesses, postExecutionProcessOptions, saveExecutionProcesses]);
  const tabs = Object(react["useMemo"])(() => {
    const result = [{
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
  const [selectedTab, setSelectedTab] = Object(react["useState"])('Pre execution process');
  const changedTabs = Object(react["useMemo"])(() => {
    const result = [];
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
  const getSelectedTab = Object(react["useCallback"])(() => {
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
  const onReset = Object(react["useCallback"])(() => {
    if (selectedTab === 'Pre execution process') {
      saveForm({
        preExecutionProcesses: []
      });
      setPreExecutionProcessOptions([...preExecutionProcessOptions]);
    } else if (selectedTab === 'Post execution process') {
      saveForm({
        postExecutionProcesses: []
      });
      setPostExecutionProcessOptions([...postExecutionProcessOptions]);
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

const TaskTitle_styles_Wrapper = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: center;

`;
const TaskTitle_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 635px;
    display: flex;
    flex-direction: column;
    gap: 32px;
`;
// CONCATENATED MODULE: ./src/containers/Task/Froms/TaskTitle/index.tsx






function TaskTitleForm(props) {
  var _errors$task_title;
  const {
    tasks_titles_active
  } = props;
  const {
    taskData,
    saveForm,
    register,
    errors,
    copy
  } = Object(react["useContext"])(TaskContext);
  const {
    task_title,
    task_description,
    task_id,
    taskGroupIds,
    task_groups
  } = taskData;
  const [taskTitleLocal, setTaskTitleLocal] = Object(react["useState"])(task_title || '');
  Object(react["useEffect"])(() => {
    setTaskTitleLocal(task_title || '');
  }, [task_title]);
  const taskTitleChange = Object(react["useCallback"])(taskTitle => {
    setTaskTitleLocal(taskTitle);
    saveForm({
      task_title: taskTitle,
      manual_title_change: true
    });
  }, [saveForm]);
  const validateTaskTitle = value => {
    if (task_id) {
      return true;
    }
    if (tasks_titles_active && tasks_titles_active.indexOf(value || '') >= 0) {
      return `Task # ${value} Already Exists`;
    }
    return true;
  };
  const taskDescriptionChange = Object(react["useCallback"])(value => {
    saveForm({
      task_description: value
    });
  }, [saveForm]);
  const taskGroupChange = Object(react["useCallback"])(value => {
    console.log(value);
    saveForm({
      taskGroupIds: (value === null || value === void 0 ? void 0 : value.map(it => it.value)) || []
    });
  }, [saveForm]);
  const task_groups_value = Object(react["useMemo"])(() => {
    if (!taskGroupIds || taskGroupIds.length == 0) {
      return [];
    }
    return task_groups === null || task_groups === void 0 ? void 0 : task_groups.filter(it => (taskGroupIds === null || taskGroupIds === void 0 ? void 0 : taskGroupIds.indexOf(it.value)) >= 0);
  }, [task_groups, taskGroupIds]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTitle_styles_Wrapper, {
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskTitle_styles_Container, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
        ...register('task_title', {
          required: 'Please input a task title',
          pattern: {
            value: /^((?!_).)*$/,
            message: "Task title must not contain '_'"
          },
          validate: {
            taskTitleExist: validateTaskTitle
          }
        }),
        disabled: task_id && !copy,
        name: "task_title",
        placeholder: "Enter task name",
        mandatory: false,
        type: InputTypes.text,
        value: taskTitleLocal,
        onChange: taskTitleChange,
        title: "Task name",
        error: (_errors$task_title = errors.task_title) === null || _errors$task_title === void 0 ? void 0 : _errors$task_title.message
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TextArea, {
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
const groupByField = (data, field) => {
  return data.reduce((acc, curr) => {
    if (!acc[curr[field]]) acc[curr[field]] = []; //If this type wasn't previously stored
    acc[curr[field]].push(curr);
    return acc;
  }, {});
};
const uniqueByField = (data, field) => {
  return data.filter((item, index, array) => {
    return array.findIndex(it => it[field] === item[field]) === index;
  });
};
// CONCATENATED MODULE: ./src/containers/Task/Main/useLogicalUnits.ts




const useLogicalUnits = (initFinished, saveForm, initTask, dataSourceType, source_type, selected_logical_units_names, be_type, be_id, source_environment_id, environment_id) => {
  const toast = hooks_useToast();
  const [sourceLogicalUnits, setSourceLogicalUnits] = Object(react["useState"])([]);
  const [targetLogicalUnits, setTargetLogicalUnits] = Object(react["useState"])([]);
  const [allLogicalUnits, setAllLogicalUnits] = Object(react["useState"])([]);
  const getLogicalUnits = Object(react["useCallback"])(async (be_id, setter, environment_id) => {
    try {
      const data = await apis_task.getLogicalUnits(be_id, environment_id);
      data.forEach(item => {
        item.value = item.lu_id;
        item.label = item.lu_name;
      });
      setter(data);
    } catch (err) {
      // use hook toast
      console.error(err);
    }
  }, []);
  Object(react["useEffect"])(() => {
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
  Object(react["useEffect"])(() => {
    if (!initFinished) {
      return;
    }
    if (be_id) {
      if (be_type === 'target' || environment_id) {
        getLogicalUnits(be_id, setTargetLogicalUnits, environment_id);
      }
    }
  }, [environment_id, initFinished, dataSourceType, be_type]);
  Object(react["useEffect"])(() => {
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
  Object(react["useEffect"])(() => {
    const temp = (sourceLogicalUnits || []).concat(targetLogicalUnits || []);
    let allLus = uniqueByField(temp, 'lu_id').filter(it => {
      const sourceResult = sourceLogicalUnits.findIndex(sourceItem => sourceItem.lu_id === it.lu_id) >= 0;
      const targeResult = targetLogicalUnits.findIndex(targetItem => targetItem.lu_id === it.lu_id) >= 0;
      return (sourceResult || sourceLogicalUnits.length === 0) && (targeResult || targetLogicalUnits.length === 0);
    });
    setAllLogicalUnits(allLus);
    const removedLus = (selected_logical_units_names || []).filter(it => allLus.findIndex(it2 => it2.lu_name === it) < 0);
    if (removedLus.length > 0 && !initTask) {
      if (dataSourceType !== 'data_source' || source_type !== 'tables') {
        if (be_type === 'source' && source_environment_id || be_type === 'target' && environment_id) {
          toast.warning(`The selected env does not contain the ${removedLus} ${removedLus.length === 1 ? 'system' : 'systems'}.`, true);
        }
      }
    }
    if (selected_logical_units_names && selected_logical_units_names.length > 0) {
      const tempLus = allLus.filter(it => selected_logical_units_names.indexOf(it.lu_name) >= 0);
      if (tempLus.length > 0) {
        allLus = tempLus;
      }
    }
    saveForm({
      selected_logical_units: allLus.map(it => it.lu_id),
      selected_logical_units_names: allLus.map(it => it.lu_name)
    });
  }, [sourceLogicalUnits, targetLogicalUnits]);
  return allLogicalUnits;
};
/* harmony default export */ var Main_useLogicalUnits = (useLogicalUnits);
// CONCATENATED MODULE: ./src/containers/Task/Main/utils.ts



const defaultValues = ['task_title', 'be_id', 'task_id', 'be_name', 'environment_id', 'environment_name', 'replace_sequences', 'scheduler', 'scheduling_end_date', 'source_environment_id', 'source_environment_name', 'selection_method', 'version_ind', 'sync_mode', 'selection_param_value', 'num_of_entities', 'reserve_note', 'parameters', 'filterout_reserved', 'retention_period_type', 'retention_period_value', 'selected_version_task_name', 'selected_version_task_exe_id', 'delete_before_load', 'reserve_ind', 'clone_ind', 'load_entity', 'task_description', 'reserve_retention_period_type', 'reserve_retention_period_value', 'globals', 'tableList', 'custom_logic_lu_name', 'selected_subset_task_exe_id', 'task_status', 'task_created_by', 'owners', 'task_execution_status', 'execution_mode', 'taskGroupIds', 'evaluation_ind'];
const fieldsMapper = {
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

const convertTaskData = (apiData, copy) => {
  let taskData = {
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
    generateChosenParams: [],
    evaluation_ind: false
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
  defaultValues.forEach(field => {
    taskData[field] = apiData[field];
  });
  if (taskData.clone_ind) {
    taskData.num_of_clones = apiData.num_of_entities;
  }
  if (taskData.selection_method === 'CLONE') {
    taskData.selection_method = SelectionMethodEnum.L;
  }
  Object.keys(fieldsMapper).forEach(field => {
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
  if (taskData.num_of_entities === -1) {
    taskData.num_of_entities = undefined;
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

const updateTaskType = (taskData, data) => {
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
const prepareDataForSave = (taskData, logicalUnits, copy) => {
  const data = {};
  const fieldsToCopy = ['task_id', 'be_id', 'postExecutionProcesses', 'preExecutionProcesses', 'environment_id', 'environment_name', 'source_environment_id', 'scheduler', 'num_of_entities', 'selection_method', 'selection_param_value', 'task_title', 'parameters', 'scheduling_end_date', 'version_ind', 'retention_period_value', 'retention_period_type', 'reserve_retention_period_type', 'reserve_retention_period_value', 'reserve_ind', 'load_entity', 'clone_ind', 'delete_before_load', 'reserve_note', 'selected_version_task_name', 'selected_version_datetime', 'selected_version_task_exe_id', 'filterout_reserved', 'mask_sensitive_data', 'replace_sequences', 'task_description', 'sync_mode', 'globals', 'reference', 'selected_ref_version_task_name', 'selected_ref_version_task_exe_id', 'selected_ref_version_datetime', 'task_globals', 'selected_subset_task_exe_id', 'custom_logic_lu_name', 'execution_mode', 'taskGroupIds', 'evaluation_ind'];
  fieldsToCopy.forEach(key => {
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
  const selectedLogicalUnits = logicalUnits.filter(it => ((taskData === null || taskData === void 0 ? void 0 : taskData.selected_logical_units) || []).indexOf(it.lu_id) >= 0);
  data.logicalUnits = selectedLogicalUnits === null || selectedLogicalUnits === void 0 ? void 0 : selectedLogicalUnits.map(it => ({
    lu_name: it.lu_name,
    lu_id: it.lu_id
  }));
  if ((taskData.clone_ind || taskData.replace_sequences) && !taskData.load_entity || taskData.target_env === 'ai_training' || !taskData.environment_id || !(taskData.sync_mode === 'OFF' && taskData.version_ind) && taskData.selection_method === 'ALL') {
    data.filterout_reserved = 'NA';
  }
  if (taskData.maxToCopy === 9007199254740992 && !taskData.num_of_entities) {
    data.num_of_entities = -1;
  }
  return data;
};
const getIfTables = taskData => {
  if (taskData.dataSourceType === 'data_source') {
    if (taskData.source_type === 'tables') {
      return 'tables';
    } else if (taskData.tables_selected) {
      return 'be_tables';
    }
  }
  return '';
};
const getSourceInfo = taskData => {
  const result = [];
  const isTables = getIfTables(taskData);
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
      result.push(`Generate ${taskData.num_of_entities || 0} entities.`);
    } else {
      result.push('Get generated entities from the TDM Data store');
    }
  } else if (taskData.dataSourceType === 'ai_generated') {
    if (taskData.synthetic_type === 'new_data') {
      result.push(`Generate ${taskData.num_of_entities || 0} entities.`);
    } else {
      result.push('Get generated entities from the TDM Data store');
    }
  }
  return result;
};
const getTargetInfo = taskData => {
  const result = [];
  const isTables = getIfTables(taskData);
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
        result.push(`Create ${taskData.num_of_entities || 0} entity clones and load tables. `);
      } else {
        result.push(`Create ${taskData.num_of_entities || 0} entity clones`);
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
        result.push(`Create ${taskData.num_of_entities} entity clones and load the related tables. Reserve the newly created entity clones.`);
      } else {
        result.push(`Create ${taskData.num_of_entities} entity clones. Reserve the newly created entity clones`);
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
      result.push(`Delete the entity from the target and create in the target ${taskData.num_of_entities} clones for the entity`);
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
      result.push(`Delete the entity from the target and create in the target ${taskData.num_of_entities} clones for the entity. Reserve the newly created entity clones`);
    }
  } else if (!taskData.load_entity && !taskData.reserve_ind && taskData.delete_before_load) {
    result.push('Delete entities');
  } else if (!taskData.load_entity && taskData.reserve_ind && !taskData.delete_before_load) {
    result.push('Reserve entities');
  }
  return result;
};
const getSubsetInfo = taskData => {
  const result = [];
  const isTables = getIfTables(taskData);
  if (isTables === 'tables') {
    var _ref;
    result.push(`Get ${(taskData.tableList || []).length} tables. `);
    const filteredTables = (_ref = taskData.tableList || []) === null || _ref === void 0 ? void 0 : _ref.filter(it => it.table_filter && it.table_filter !== '()');
    if (filteredTables.length > 0) {
      result.push(`Number of tables with data filtering: ${filteredTables.length}. `);
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
    const selection_method = taskData.selection_method !== SelectionMethodEnum.PR ? taskData.selection_method : SelectionMethodEnum.P;
    const foundSelectionMethod = entitySeletionMethods.find(it => it.value === selection_method);
    if (foundSelectionMethod && taskData.num_of_entities && foundSelectionMethod.label) {
      result.push(`Get ${taskData.num_of_entities || '(empty)'} entities based on a/an ${foundSelectionMethod.label} selection method.`);
    }
  }
  return result;
};
const getTestDataStoreInfo = (taskData, testDataStoreStatus, subsetStatus, subsetPosition) => {
  if (subsetPosition === SubsetPossition.target && subsetStatus !== StatusEnum.disabled) {
    return ['Get data from the TDM Data store.'];
  }
  if (testDataStoreStatus !== StatusEnum.completed) {
    return [];
  }
  const result = [];
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
const getTaskTitle = taskData => {
  const {
    be_name,
    be_id,
    tables_selected,
    manual_title_change,
    task_title
  } = taskData;
  if (manual_title_change) {
    return task_title || '';
  }
  let new_task_title = ``;
  console.log(be_name);
  if (be_id === -1) {
    new_task_title = new_task_title + `tables `;
  } else if (be_id) {
    if (tables_selected) {
      new_task_title = new_task_title + `${be_name} and tables `;
    } else {
      new_task_title = new_task_title + `${be_name} `;
    }
  } else {
    return '';
  }
  new_task_title = new_task_title + moment_default()(new Date()).format('DD-MM-YYYY HH:mm:ss');
  return new_task_title;
};
// CONCATENATED MODULE: ./src/components/task/TaskActions/styles.ts

const TaskActions_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 300px;
    position: absolute;
    top: 29px;
    left: 50px;
`;
const ButtonsContainer = styled_components_browser_esm["b" /* default */].div`
    height: 60px;
    display: flex;
    margin-top: 16px;
    border-left: 2px solid #e5e5e5;
`;
const Action = styled_components_browser_esm["b" /* default */].div`
    width: 99px;
    display: ${props => props.hide ? 'none' : 'flex'};
    flex-direction: column;
    gap: 7px;
    align-items: center;
    padding: 0px 15px;
    border-right: 2px solid #e5e5e5;
    cursor: pointer;
`;
const ActionText = styled_components_browser_esm["b" /* default */].div`
  font-family: Roboto;
  font-size: 12px;
  font-weight: 500;
  font-stretch: normal;
  font-style: normal;
  line-height: 1;
  letter-spacing: normal;
  text-align: center;
  color: #2e2e2e;

`;
const TaskTitleContainer = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    font-family: Roboto;
    font-size: 15px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    padding-bottom: 17px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
`;
const EntitiesAndTablesContainer = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    min-height: 39px;
    display: flex;
    gap: 15px;
    align-items: center;
    font-family: Roboto;
    font-size: 15px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    border-bottom: 2px solid #e5e5e5;
    padding-bottom: 17px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
`;
const TaskTitleButton = styled_components_browser_esm["b" /* default */].span`
    color: #1683f2;
`;
const TaskActions_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    width: 25px;
`;
// CONCATENATED MODULE: ./src/images/settings-icon.svg
/* harmony default export */ var settings_icon = ("js/dist/c91068a2c13098ab1c04ec54f77536a9.svg");
// CONCATENATED MODULE: ./src/images/save-icon.svg
/* harmony default export */ var save_icon = ("js/dist/a5e1a483f3b25d0de3786b1759104171.svg");
// CONCATENATED MODULE: ./src/images/save-exe-icon.svg
/* harmony default export */ var save_exe_icon = ("js/dist/a6172cf3de51586156f0bf4ab2d4d11d.svg");
// CONCATENATED MODULE: ./src/components/task/TaskActions/index.tsx







function TaskActions(props) {
  const {
    setCurrentStep,
    task_title,
    saveLocalData,
    closeTask,
    saveTask,
    saveAndExecute,
    deleteTask,
    tables_selected,
    be_name,
    disableChange,
    task_execution_status
  } = props;
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskActions_styles_Container, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskTitleContainer, {
      onClick: () => setCurrentStep('task_title'),
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
        onClick: () => saveTask(),
        hide: disableChange,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
          src: save_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionText, {
          children: "Save"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Action, {
        onClick: () => saveAndExecute(),
        hide: disableChange || task_execution_status == 'onHold',
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskActions_styles_Icon, {
          src: save_exe_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(ActionText, {
          children: "Save & execute"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(Action, {
        onClick: () => setCurrentStep('be_advanced'),
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




const useRoles = (saveForm, taskData) => {
  const authService = getService('AuthService');
  const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
  const userId = authService === null || authService === void 0 ? void 0 : authService.getUserId();
  const [userFabricRoles, setUserFabricRoles] = Object(react["useState"])(null);
  Object(react["useEffect"])(() => {
    async function fetchFabricRoles() {
      const data = await apis_task.getFabricRolesByUser(userId);
      setUserFabricRoles(data || []);
    }
    fetchFabricRoles();
  }, []);
  const {
    source_environment_id,
    environment_id,
    reserve_ind,
    delete_before_load,
    load_entity,
    targetEnvOwner,
    sourceEnvOwner,
    maxToCopy,
    maxToCopyType,
    userRole,
    sync_mode
  } = taskData;
  const getRoleForUserInEnv = Object(react["useCallback"])(async (env_id, isSource) => {
    if (isSource && sourceEnvOwner || !isSource && targetEnvOwner) {
      return;
    }
    const data = await apis_task.getEnvironmentUserRole(env_id);
    const updateData = {
      [isSource ? 'sourceUserRole' : 'userRole']: data.userRole
    };
    updateData[isSource ? 'sourceUserRole' : 'userRole'].userType = 'tester';
    const temp_data = {};
    updateTaskType(taskData, temp_data);
    const {
      task_type
    } = temp_data;
    let minRead = parseInt(data.minRead || '0');
    let minWrite = parseInt(data.minWrite || '0');
    if (minRead === -1) {
      minRead = 9007199254740992;
    }
    if (minWrite === -1) {
      minWrite = 9007199254740992;
    }
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
  }, [sourceEnvOwner, targetEnvOwner, maxToCopy, maxToCopyType, userRole, saveForm, reserve_ind, load_entity, sync_mode]);
  const getEnvironmentOwners = Object(react["useCallback"])(async (env_id, isSource) => {
    const data = await apis_task.getEnvironmentOwners(env_id);
    let ownerFound = data.find(it => it.user_id === userId);
    if (!ownerFound) {
      for (let i = 0; i < userFabricRoles.length; i++) {
        const result = data.find(it => it.user_type === 'GROUP' && it.user_id === userFabricRoles[i]);
        if (result) {
          ownerFound = result;
          break;
        }
      }
    }
    if (ownerFound) {
      saveForm({
        [isSource ? 'sourceUserRole' : 'userRole']: {
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
        },
        maxToCopy: 9007199254740992,
        [isSource ? 'sourceEnvOwner' : 'targetEnvOwner']: true
      });
      return isSource ? 'source_owner' : 'target_owner';
    } else {
      saveForm({
        [isSource ? 'sourceEnvOwner' : 'targetEnvOwner']: false
      });
      return isSource ? 'not_source_owner' : 'not_target_owner';
      ;
    }
  }, [userFabricRoles, saveForm, userId]);
  Object(react["useEffect"])(() => {
    if (reserve_ind && !load_entity && userRole && userRole.userType === 'tester') {
      saveForm({
        maxToCopy: userRole.allowed_number_of_reserved_entities
      });
    }
  }, [reserve_ind]);
  Object(react["useEffect"])(() => {
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
      const promises = [];
      if (!source_environment_id) {
        saveForm({
          sourceUserRole: undefined
        });
      } else if (source_environment_id && !(reserve_ind && !load_entity || delete_before_load && !load_entity) && environment_id != source_environment_id) {
        promises.push(getEnvironmentOwners(source_environment_id, true));
      }
      if (environment_id) {
        promises.push(getEnvironmentOwners(environment_id, false));
      } else {
        saveForm({
          userRole: undefined
        });
      }
      Promise.all(promises).then(async result => {
        if (result.indexOf('not_source_owner') >= 0 && source_environment_id && !(reserve_ind && !load_entity || delete_before_load && !load_entity)) {
          await getRoleForUserInEnv(source_environment_id, true);
        }
        if (result.indexOf('not_target_owner') >= 0 && environment_id) {
          await getRoleForUserInEnv(environment_id, false);
        }
      });
    }
  }, [userFabricRoles, source_environment_id, environment_id, load_entity, reserve_ind, delete_before_load, sync_mode]);
  return {};
};
/* harmony default export */ var Main_useRoles = (useRoles);
// CONCATENATED MODULE: ./src/containers/Task/Main/useInit.ts


const useInit = (saveForm, taskData) => {
  Object(react["useEffect"])(() => {
    async function fetchActiveBE() {
      try {
        const data = await apis_task.getActiveBEs();
        const updateData = {
          enable_masking_only: !data || data.length === 0 ? true : false
        };
        if (updateData.enable_masking_only) {
          updateData.dataSourceType = 'data_source';
          updateData.source_type = 'tables';
          updateData.selection_method = 'TABLES';
          updateData.be_id = -1;
        }
        saveForm(updateData);
      } catch (err) {}
    }
    fetchActiveBE();
  }, []);
  Object(react["useEffect"])(() => {
    async function fetchEnableParamsLUName() {
      try {
        const result = await apis_task.getParamsLUName();
        saveForm({
          enable_param_lu_name: result === "true"
        });
      } catch (err) {}
    }
    async function fetchEnableAdvancedSystemsForTesters() {
      try {
        const result = await apis_task.getTaskLuEditForTesters();
        saveForm({
          enable_advanced_for_testers: result === "true"
        });
      } catch (err) {}
    }
    fetchEnableParamsLUName();
    fetchEnableAdvancedSystemsForTesters();
  }, []);
  const [fetchCounter, setFetchCounter] = Object(react["useState"])(8);
  const [finished, setFinished] = Object(react["useState"])(true);
  Object(react["useEffect"])(() => {
    async function fetchEnableParamWidth() {
      try {
        const result = await apis_task.getEnableParamWidth();
        saveForm({
          enable_param_auto_width: result === "true"
        });
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchParamCoupling() {
      try {
        const coupling_result = await apis_task.getCheckIfParamsCoupling();
        saveForm({
          isCoupling: coupling_result === "true"
        });
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    if (!taskData.task_id) {
      fetchEnableParamWidth();
      fetchParamCoupling();
      setFetchCounter(2);
      return;
    }
    const task_id = taskData.task_id;
    async function fetchTaskPostExecutionProcess() {
      try {
        const data = await apis_task.getTaskPostExecutionProcess(task_id);
        saveForm({
          postExecutionProcesses: data.map(it => {
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
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchTaskVariables() {
      try {
        const data = await apis_task.getTaskVariables(task_id);
        saveForm({
          globals: data
        });
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchTaskPreExecutionProcess() {
      try {
        const data = await apis_task.getTaskPreExecutionProcess(task_id);
        saveForm({
          preExecutionProcesses: data.map(it => {
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
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchTaskTables() {
      try {
        if (taskData.refcount === 0) {
          setFetchCounter(prevCount => prevCount - 1);
          return;
        }
        const data = await apis_task.getTaskTables(task_id);
        saveForm({
          tableList: data.map(it => ({
            ...it,
            filter_parameters: it.filter_parameters ? it.filter_parameters.split("<#>") : it.filter_parameters,
            reference_table_name: it.ref_table_name
          }))
        });
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchSourceEnvironment() {
      try {
        if (!taskData.source_environment_id) {
          setFetchCounter(prevCount => prevCount - 1);
          return;
        }
        const data = await apis_task.getEnvironmentByID(taskData.source_environment_id);
        if (data && data[0]) {
          saveForm({
            mask_sensitive_data: data[0].mask_sensitive_data
          });
        }
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
    }
    async function fetchLogicalUntis() {
      try {
        const selectedData = await apis_task.getTaskLogicalUnits(taskData.task_id || 0);
        saveForm({
          selected_logical_units: selectedData.map(it => it.lu_id),
          selected_logical_units_names: selectedData.map(it => it.lu_name)
        });
        setFetchCounter(prevCount => prevCount - 1);
      } catch (err) {}
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
  Object(react["useEffect"])(() => {
    if (fetchCounter === 0) {
      setFinished(true);
    }
  }, [fetchCounter]);
  return {
    initFinished: finished
  };
};
/* harmony default export */ var Main_useInit = (useInit);
// CONCATENATED MODULE: ./src/containers/Task/Main/useGenerationParams.ts


const useGenerationParams = (saveForm, dataSourceType, task_id, selected_logical_units_names, generateParams) => {
  const getDataGenerationParams = Object(react["useCallback"])(async () => {
    if (dataSourceType !== 'synthetic') {
      return;
    }
    console.log('getDataGenerationParams');
    const data = await apis_task.getDataGenerationParams(task_id, selected_logical_units_names || []);
    const updateData = {};
    const selectedParams = [];
    Object.keys(data || {}).forEach(key => {
      let newValueAdded = false;
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
          key,
          order: data[key].order
        });
      }
    });
    updateData.generateChosenParams = selectedParams.sort((a, b) => (a.order || 99999999) - (b.order || 99999999)).map(it => it.key);
    updateData.dataGenerationParams = data;
    saveForm(updateData);
  }, [saveForm, task_id, selected_logical_units_names, generateParams, dataSourceType]);
  Object(react["useEffect"])(() => {
    if (selected_logical_units_names && selected_logical_units_names.length > 0) {
      getDataGenerationParams();
    }
  }, [selected_logical_units_names]);

  // need to add code for tester
};
/* harmony default export */ var Main_useGenerationParams = (useGenerationParams);
// EXTERNAL MODULE: ./node_modules/sweetalert2/dist/sweetalert2.all.js
var sweetalert2_all = __webpack_require__(56);
var sweetalert2_all_default = /*#__PURE__*/__webpack_require__.n(sweetalert2_all);

// EXTERNAL MODULE: ./node_modules/sweetalert2-react-content/dist/sweetalert2-react-content.umd.js
var sweetalert2_react_content_umd = __webpack_require__(57);
var sweetalert2_react_content_umd_default = /*#__PURE__*/__webpack_require__.n(sweetalert2_react_content_umd);

// CONCATENATED MODULE: ./src/containers/Task/Main/useExecutionMode.ts


const useExecutionMode = (initFinished, taskData) => {
  const toast = hooks_useToast();
  const {
    clone_ind,
    execution_mode,
    be_execution_mode
  } = taskData;
  Object(react["useEffect"])(() => {
    if (initFinished) {
      if (clone_ind && (execution_mode === 'VERTICAL' || execution_mode === 'INHERITED' && be_execution_mode === 'VERTICAL')) {
        toast.warning('The task execution will run in a horizontal mode since the entity clone does not support the vertical execution mode.');
      }
    }
  }, [clone_ind, execution_mode]);
};
/* harmony default export */ var Main_useExecutionMode = (useExecutionMode);
// CONCATENATED MODULE: ./src/containers/Task/Main/index.tsx





























const MySwal = sweetalert2_react_content_umd_default()(sweetalert2_all_default.a);
function TaskMain(props) {
  const {
    mode,
    task,
    openTasks,
    copy,
    tasks,
    scope
  } = props.content;
  const toast = hooks_useToast();
  const [taskData, setTaskData] = Object(react["useState"])(convertTaskData(task, copy));
  const [saveInProgress, setSaveInProgress] = Object(react["useState"])(false);
  const [failedComp, setFailedComp] = Object(react["useState"])('');
  const [disableChange, setDisableChange] = Object(react["useState"])(false);
  const [task_titles, setTaskTitles] = Object(react["useState"])(tasks && tasks.filter(it => it.task_status === 'Active').map(it => it.task_title || '') || []);
  Object(react["useEffect"])(() => {
    let disableChangeLocal = false;
    if (taskData.task_id) {
      const authService = getService('AuthService');
      const username = (authService === null || authService === void 0 ? void 0 : authService.getUsername()) || '';
      const userRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
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
  Object(react["useEffect"])(() => {
    setTaskTitles(tasks && tasks.map(it => it.task_title || '') || []);
  }, [tasks]);
  Object(react["useEffect"])(() => {
    // if (!taskData.task_title) {
    if (!taskData.task_id) {
      const task_title = getTaskTitle(taskData);
      saveForm({
        task_title
      });
    }
    // }
  }, [taskData.be_id, taskData.tables_selected, taskData.be_name]);
  Object(react["useEffect"])(() => {
    const BreadCrumbsService = getService('BreadCrumbsService');
    if (BreadCrumbsService) {
      BreadCrumbsService.push({
        task_id: taskData.task_title
      }, 'TASK_BREADCRUMB', function () {});
    }
  }, []);
  const {
    resetField,
    register,
    unregister,
    setValue,
    handleSubmit,
    trigger,
    formState: {
      errors,
      isValid
    },
    clearErrors
  } = Object(index_esm["b" /* useForm */])({
    defaultValues: taskData
  });
  const {
    statuses,
    onClickStep,
    currentStep,
    touchedForms,
    setTouchedForms,
    submittedForm,
    setSubmittedForm,
    statusesFuncMap,
    initTask
  } = Main_useWidgetStatus(taskData, trigger, isValid, handleSubmit, failedComp);
  const saveForm = Object(react["useCallback"])(data => {
    if (failedComp === currentStep) {
      setFailedComp('');
    }
    setTaskData(previousTaskData => ({
      ...previousTaskData,
      ...data
    }));
    Object.keys(data).forEach(key => {
      setValue(key, data[key]);
      clearErrors(key);
    });
  }, [setValue, clearErrors, setFailedComp, failedComp, currentStep]);
  const {
    initFinished
  } = Main_useInit(saveForm, taskData);
  const allLogicalUnits = Main_useLogicalUnits(initFinished, saveForm, initTask, taskData === null || taskData === void 0 ? void 0 : taskData.dataSourceType, taskData === null || taskData === void 0 ? void 0 : taskData.source_type, taskData === null || taskData === void 0 ? void 0 : taskData.selected_logical_units_names, taskData === null || taskData === void 0 ? void 0 : taskData.be_type, taskData === null || taskData === void 0 ? void 0 : taskData.be_id, taskData === null || taskData === void 0 ? void 0 : taskData.source_environment_id, taskData === null || taskData === void 0 ? void 0 : taskData.environment_id);
  Main_usePeriods(saveForm, taskData.version_ind, taskData.dataSourceType, taskData.source_type, taskData.retention_period_value, taskData.retention_period_type, taskData.reserve_retention_period_value, taskData.reserve_retention_period_type);
  Main_useExecutionMode(initFinished, taskData);
  Main_useRoles(saveForm, taskData);
  Main_useGenerationParams(saveForm, taskData.dataSourceType, taskData.task_id, taskData.selected_logical_units_names, taskData.dataGenerationParams);
  const onReset = Object(react["useCallback"])(() => {
    const authService = getService('AuthService');
    const systemUserRole = authService === null || authService === void 0 ? void 0 : authService.getRole();
    if (currentStep === 'source_data_subset' || currentStep === 'target_data_subset') {
      if (taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables') {
        const tableList = (taskData.tableList || []).map(it => {
          it.gui_filter = undefined;
          it.filter_type = undefined;
          it.table_filter = undefined;
          return it;
        });
        saveForm({
          subsetReset: true,
          tableList: [...tableList]
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
      const updateData = {
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
        load_entity: taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables' || taskData.version_ind && !(taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables') ? true : false,
        reserve_ind: false,
        delete_before_load: taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables' || taskData.version_ind && !(taskData.dataSourceType === 'data_source' && taskData.source_type === 'tables') ? true : false,
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
      setTouchedForms(oldArray => oldArray.filter(it => it !== currentStep));
    }
    setTimeout(() => {
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
  const getCurrentForm = Object(react["useCallback"])(() => {
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
  const getSourceName = Object(react["useCallback"])(() => {
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
  const getSourceSubTitle = Object(react["useCallback"])(() => {
    if (taskData.dataSourceType === 'ai_generated') {
      return taskData.selected_training_name || ' ';
    } else if (taskData.dataSourceType === 'synthetic') {
      return ' ';
    }
    return '';
  }, [taskData]);
  const getTargetName = Object(react["useCallback"])(() => {
    if (taskData.target_env === 'ai_training') {
      return 'AI Training';
    }
    return taskData.environment_name;
  }, [taskData]);
  const moveToMadatoryForms = Object(react["useCallback"])(() => {
    const source = statusesFuncMap['dataSourceStatus'](taskData);
    const target = statusesFuncMap['targetStatus'](taskData);
    const subsetPosition = statusesFuncMap['subsetPosition'](taskData);
    const subsetStatus = statusesFuncMap['subsetStatus'](taskData);
    const moveTo = step => {
      // @ts-ignore
      setFailedComp(step);
      onClickStep(step);
    };
    const testDataStoreStatus = statusesFuncMap['testDataStoreStatus'](taskData);
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
  const isValidTaskData = Object(react["useCallback"])(() => {
    console.log(failedComp);
    const source = statusesFuncMap['dataSourceStatus'](taskData);
    const target = statusesFuncMap['targetStatus'](taskData, statuses);
    const subsetPosition = statusesFuncMap['subsetPosition'](taskData);
    const subsetStatus = statusesFuncMap['subsetStatus'](taskData);
    const testDataStoreStatus = statusesFuncMap['testDataStoreStatus'](taskData);
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
  const handleFormErrors = Object(react["useCallback"])(async () => {
    const fieldErrors = Object.keys(errors);
    if (fieldErrors.length > 0) {
      // @ts-ignore
      setFailedComp(errors[fieldErrors[0]].ref.step);
      // @ts-ignore
      onClickStep(errors[fieldErrors[0]].ref.step);
      return true;
    } else {
      await setFailedComp('');
    }
    return false;
  }, [onClickStep, errors, setFailedComp]);
  const saveTask = Object(react["useCallback"])(async openTask => {
    if (saveInProgress) {
      return;
    }
    await setSubmittedForm(true);
    const formResult = await handleFormErrors();
    if (formResult) {
      toast.error(`All mandatory information must be completed before saving the task`);
      return false;
    }
    if (!isValidTaskData()) {
      toast.error(`All mandatory information must be completed before saving the task`);
      moveToMadatoryForms();
      return false;
    }
    let result = null;
    if (taskData.version_ind && taskData.selected_version_task_name) {
      if ((taskData.selected_version_succeeded_entities || 0) > (taskData.maxToCopy || 0)) {
        toast.error('The number of entities exceeds the number of entities in the read write permission');
        onClickStep('target_data_subset');
        return false;
      }
    }
    const checkProcesss = data => {
      for (let i = 0; i < data.length; i++) {
        const processData = data[0];
        if (processData.editors.findIndex(it => it.mandatory && (it.value === undefined || it.value === null)) >= 0) {
          return true;
        }
      }
      return false;
    };
    if (taskData.preExecutionProcesses && taskData.preExecutionProcesses.length > 0 && checkProcesss(taskData.preExecutionProcesses)) {
      toast.error('Mandatory info is missing for the pre-execution process');
      onClickStep('be_advanced');
      return false;
    }
    if (taskData.postExecutionProcesses && taskData.postExecutionProcesses.length > 0 && checkProcesss(taskData.postExecutionProcesses)) {
      toast.error('Mandatory info is missing for the post-execution process');
      onClickStep('be_advanced');
      return false;
    }
    const dataForSave = prepareDataForSave(taskData, allLogicalUnits, copy);
    if (false) {}
    setSaveInProgress(true);
    try {
      const AuthService = getService('AuthService');
      const role = AuthService === null || AuthService === void 0 ? void 0 : AuthService.getRole();
      if (role && role.type === 'tester' && dataForSave.version_ind && dataForSave.retention_period_type === 'Do Not Delete') {
        const result = await MySwal.fire({
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
        }).then(value => {
          console.log(value);
          return value.isConfirmed;
        }).catch(() => false);
        if (!result) {
          setSaveInProgress(false);
          return;
        }
      }
      if (dataForSave.task_type !== 'EXTRACT' && dataForSave.selection_method === 'L' && !dataForSave.replace_sequences && dataForSave.filterout_reserved !== 'NA') {
        const validateReserveEntitiesResult = await apis_task.validateReservedEntitiesList(dataForSave.be_id, dataForSave.environment_id, dataForSave.selection_param_value.split(',').map(it => ({
          target_entity_id: it
        })), dataForSave.filterout_reserved);
        if (validateReserveEntitiesResult.length > 0) {
          const result = await MySwal.fire({
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
          }).then(value => {
            console.log(value);
            return value.isConfirmed;
          }).catch(() => false);
          console.log(result);
          if (!result) {
            setSaveInProgress(false);
            return;
          }
        }
      }
      result = await apis_task.saveTaskAPI(dataForSave);
      toast.success(`Task # ${taskData.task_title} Is Updated Successfully`);
      if (!openTask) {
        openTasks(true);
      }
    } catch (err) {
      console.log(err);
      toast.error(`Task # ${taskData.task_id || ''} Failed to Update : ${err.message}`);
    } finally {
      setSaveInProgress(false);
    }
    return result;
  }, [taskData, saveInProgress, handleFormErrors, openTasks, copy, setSaveInProgress, allLogicalUnits, toast, moveToMadatoryForms, setSubmittedForm, isValidTaskData]);
  const saveAndExecute = Object(react["useCallback"])(async () => {
    const result = await saveTask(true);
    if (result) {
      openTasks(true, result === null || result === void 0 ? void 0 : result.id);
    }
  }, [saveTask, openTasks]);
  const [triggerSave, setTriggerSave] = Object(react["useState"])({
    execute: false,
    trigger: false
  });
  Object(react["useEffect"])(() => {
    if (triggerSave && triggerSave.trigger) {
      handleSubmit(() => {
        if (triggerSave.execute) {
          saveAndExecute();
        } else {
          saveTask();
        }
      }, data => {
        const fields = Object.keys(data);
        if (fields && fields.length > 0) {
          if (fields[0] === 'task_title') {
            onClickStep('task_title');
          } else {
            onClickStep(data[fields[0]].ref.step);
          }
        }
        ;
        console.log(data);
        toast.error(`All mandatory information must be completed before saving the task`);
      })();
    }
  }, [triggerSave]);
  const preSaveTask = Object(react["useCallback"])(execute => {
    setFailedComp('');
    setTriggerSave({
      execute: execute || false,
      trigger: true
    });
  }, [setTriggerSave, setFailedComp]);
  const deleteTask = Object(react["useCallback"])(async () => {
    if (!taskData.task_id || !taskData.task_title) {
      return;
    }
    await apis_task.deleteTask(taskData.task_id, taskData.task_title);
    openTasks(true);
  }, [taskData, openTasks]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Container, {
    className: "react-comp",
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(task_TaskActions, {
      setCurrentStep: onClickStep,
      task_title: taskData.task_title,
      saveLocalData: saveForm,
      task_id: taskData.task_id,
      be_name: taskData.be_name,
      tables_selected: taskData.tables_selected,
      saveTask: () => preSaveTask(),
      closeTask: openTasks,
      saveAndExecute: () => preSaveTask(true),
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
          subsetInfo: getSubsetInfo(taskData),
          datastoreInfo: getTestDataStoreInfo(taskData, statusesFuncMap['testDataStoreStatus'](taskData), statusesFuncMap['subsetStatus'](taskData), statusesFuncMap['subsetPosition'](taskData)),
          environment_name: getTargetName(),
          source_environment_name: getSourceName(),
          sourceSubTitle: getSourceSubTitle(),
          targetSubTitle: taskData.environment_name ? '' : ' '
        })
      })
    }), currentStep ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskContext.Provider, {
      value: {
        resetField,
        unregister,
        register,
        clearErrors,
        errors,
        submittedForm,
        saveForm,
        taskData,
        allLogicalUnits,
        statusesFuncMap,
        scope,
        copy: copy || false,
        config_params: {
          enable_param_auto_width: taskData.enable_param_auto_width
        }
      },
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(task_TaskForm, {
        ...stepsConfig[currentStep],
        onReset: onReset,
        width: stepsConfig[currentStep].width || 1590,
        children: getCurrentForm()
      })
    }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})]
  });
}
/* harmony default export */ var Main = (TaskMain);
// CONCATENATED MODULE: ./src/containers/TaskTemplates/styles.ts

const TaskTemplates_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: calc(100% - 60px);
    min-height: calc(100vh - 42px);
    background-color: white;
    position: relative; 
    padding: 30px 30px 0px 30px;
`;
const TaskTemplates_styles_LeftSide = styled_components_browser_esm["b" /* default */].div`
    width: 30%;
    margin-right: 40px; 
    margin-top: 35px;
    display: flex;
    flex-direction: column;
    gap: 45px;
`;
const LeftSideHeader = styled_components_browser_esm["b" /* default */].div`
    font-size: 20px;
    font-family: Roboto;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    color: #2e2e2e;
    padding-bottom: 10px;
`;
const LeftSideFilter = styled_components_browser_esm["b" /* default */].div`
    margin-bottom: 10px;
`;
const LeftSideTemplate = styled_components_browser_esm["b" /* default */].div`
    border-bottom: solid 1px #ccc;  
    display: flex;
    align-items: center;
    padding-left: 13px;
    padding-right: 13px;
    height: 45px;
    background-color: ${props => props.selected ? '#f2f2f2' : ''};
    font-weight: ${props => props.selected ? '500' : ''};
    &:hover > div:last-child {
        opacity: 1;
        visibility: visible;
    }
`;
const LeftSideTemplateIconContainer = styled_components_browser_esm["b" /* default */].div`
    width: 34px;
    cursor: pointer;
`;
const LeftSideTemplateName = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    font-size: 16px;
    font-family: Roboto;
    cursor: pointer;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    width: calc(100% - 60px);
`;
const LeftSideTemplateActions = styled_components_browser_esm["b" /* default */].div`
    border-left: solid 1px #ccc;
    padding-left: 14px;
    display: flex;
    gap: 14px;
`;
const TaskTemplates_styles_RightSide = styled_components_browser_esm["b" /* default */].div`
    width: 70%;
    border-left: 1px solid #ccc;
    padding: 0px 38px;
    background-color: #ececec;
    height: calc(100vh - 217px);
    overflow: auto;
`;
const RightSideHeader = styled_components_browser_esm["b" /* default */].div`
    font-size: 20px;
    font-family: Roboto;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    color: #2e2e2e;
    margin-top: 35px;
        
`;
const RightSideBody = styled_components_browser_esm["b" /* default */].div`
    // padding-top: 35px;
`;
const RightSideBox = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    width: 13.4vw;
    height: 9.3vw;
    border: 1px solid #ccc;
    padding: 0px;
    object-fit: contain;
    border-radius: 10px;
    border: solid 1px #fff;
    background-color: #fff;
    position: relative;
    margin-bottom: 40px;
    ${props => props.selected ? `
        background: 
            linear-gradient(#fff, #fff) padding-box, /* Inner background */
            linear-gradient(to top, #2c75f2, #8047f0) border-box; /* Border gradient */
        border: 1px solid transparent;
        border-radius: 10px;
        background-origin: border-box;
        background-clip: padding-box, border-box;` : ''}
    &:hover {
        background: 
            linear-gradient(#fff, #fff) padding-box, /* Inner background */
            linear-gradient(to top, #2c75f2, #8047f0) border-box; /* Border gradient */
        border: 1px solid transparent;
        border-radius: 10px;
        background-origin: border-box;
        background-clip: padding-box, border-box;
    }
`;
const RightSideBoxHeader = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: flex-start;
    justify-content: flex-end;
    margin: 10px 10px 0px 10px;
`;
const RightSideBoxBody = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 14px;
    margin-left: 20px;
    margin-top: 15px;
`;
const RightSideBoxBodyTitle = styled_components_browser_esm["b" /* default */].div`
    font-size: 22px;
    font-family: Roboto;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    color: #2e2e2e;
    width: 85%;
`;
const RightSideBoxHeaderActions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 10px;
    align-items: center;
`;
const styles_Header = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    border-bottom: 1px solid #ccc;
    height: 85px;
    display: flex;
    justify-content: space-between;
    align-items: center;
`;
const TaskTemplates_styles_Body = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    height: calc(100% - 86px);
    padding: 0px 0px 0px 0px;
    display: flex;
`;
const TaskTemplates_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 30px;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    color: #1483f3;
`;
const HeaderActions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    gap: 15px;
    align-items: center;
`;
const TaskTemplates_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: ${props => props.width ? props.width : ''};
    height: ${props => props.height ? props.height : ''}; 
`;
const SearchBar = styled_components_browser_esm["b" /* default */].div`
    width: 40%;
`;
const styles_TaskActionContainer = styled_components_browser_esm["b" /* default */].div`
    position: relative;
    padding-left: 16px;
    &:not(:first-child) {
        border-left: 2px solid #ccc; /* Adjust color and width as needed */
    }
`;
const TaskAction = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    color: #1483f3;
    cursor: pointer;
    &:hover {
        color: #f0832b;
    }
`;
const DotMenuIconWrapper = styled_components_browser_esm["b" /* default */].div`
  opacity: 0;
  display: flex;
  align-items:center;
  visibility: hidden;
  transition: opacity 0.2s ease-in-out;
  cursor: pointer;
  height: 100%;
`;
const CategoriesContainer = styled_components_browser_esm["b" /* default */].div`

`;
const TaskTypeIconContainer = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    top: -24px;
    left: 24px;
`;
const StartIconContainer = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    right: 12px;
    bottom: 12px;
`;
const BoxesContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-top: 32px;
`;
// CONCATENATED MODULE: ./src/images/selected-favorit.svg
/* harmony default export */ var selected_favorit = ("js/dist/01b5e973bce49f6dd5d13c320b1e0a57.svg");
// CONCATENATED MODULE: ./src/images/favorite-icon-new.svg
/* harmony default export */ var favorite_icon_new = ("js/dist/533c44529cca77055cf5d7e9630fbd92.svg");
// CONCATENATED MODULE: ./src/images/dotmenu.svg
/* harmony default export */ var dotmenu = ("js/dist/0ac2582f20a3f427aaa6bf0295e2360c.svg");
// CONCATENATED MODULE: ./src/images/extract.svg
/* harmony default export */ var extract = ("js/dist/e429f5c939b4f71c4541c746d8a6f044.svg");
// CONCATENATED MODULE: ./src/images/load.svg
/* harmony default export */ var load = ("js/dist/22cf4d6008735f32a68ae163af2f6456.svg");
// CONCATENATED MODULE: ./src/images/extract-and-load.svg
/* harmony default export */ var extract_and_load = ("js/dist/6fba05fba343334b2d9a9ae45ee724ba.svg");
// CONCATENATED MODULE: ./src/images/reserve.svg
/* harmony default export */ var images_reserve = ("js/dist/b63b8d5048493f3d631a1ec5d2e44f0c.svg");
// CONCATENATED MODULE: ./src/images/delete.svg
/* harmony default export */ var images_delete = ("js/dist/6ef0cbd3bdc028becf5657276c0b09ce.svg");
// CONCATENATED MODULE: ./src/images/synthetic.svg
/* harmony default export */ var synthetic = ("js/dist/6455c216f36ac6be7ae7ec940eb575ab.svg");
// CONCATENATED MODULE: ./src/images/open_execution.svg
/* harmony default export */ var open_execution = ("js/dist/4f485a6c2815258d37fe11bc17a2c8fa.svg");
// CONCATENATED MODULE: ./src/containers/TaskTemplates/TaskSelection/styles.ts

const TaskSelection_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: calc(100% - 60px);
    min-height: calc(100vh - 42px);
    background-color: white;
    position: relative; 
    padding: 30px 30px 0px 30px;
`;
const TaskSelection_styles_Header = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    border-bottom: 1px solid #ccc;
    display: flex;
    justify-content: flex-start;
    align-items: center;
`;
const TaskSelection_styles_Body = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-top: 20px;
`;
const SideContainer = styled_components_browser_esm["b" /* default */].div`
    width: 107px;
    height: 45px;
`;
const MiddleContainer = styled_components_browser_esm["b" /* default */].div`
    width: 60vw;
`;
const TaskSelection_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 30px;
    font-weight: 500;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #1483f3;
    padding-bottom: 24px;
`;
const TaskSelection_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: ${props => props.width ? props.width : ''};
    height: ${props => props.width ? props.width : ''}; 
`;
// CONCATENATED MODULE: ./src/containers/TaskTemplates/TaskSelectionBox/styles.ts

const TaskSelectionBox_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: calc(100% - 60px);
    min-height: calc(100vh - 42px);
    background-color: white;
    position: relative; 
    padding: 30px 30px 0px 30px;
`;
const TaskSelectionBox_styles_Header = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    border-bottom: 1px solid #ccc;
    display: flex;
    gap: 12px;
    align-items: flex-end;
    padding-bottom: 12px;
`;
const TaskSelectionBox_styles_Body = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 20px;
`;
const TaskSelectionBox_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
`;
const TaskBox = styled_components_browser_esm["b" /* default */].div`
    margin-top: 11px;
    border-radius: 6px;
    box-shadow: 0 0 14px 6px rgba(0, 0, 0, 0.1);
    background-color: #fff;
    padding: 30px;
`;
const TaskBoxTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 26px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #2e2e2e;
    margin-bottom: 24px;
`;
const styles_TaskActions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    gap: 15px;
`;
const ExecutionNotes = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    gap: 7px;
    align-items: flex-start;
    margin-top: 28px;
`;
const ExecutionNotesTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #2e2e2e;
`;
const styles_SideContainer = styled_components_browser_esm["b" /* default */].div`
    width: 90px;
`;
const styles_MiddleContainer = styled_components_browser_esm["b" /* default */].div`
    width: 60vw;
`;
const TaskSelectionBox_styles_Title = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 26px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #2e2e2e;
`;
const TaskDescription = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    gap: 7px;
    margin-top: 27px;
`;
const AdvancedSettings = styled_components_browser_esm["b" /* default */].div`
    cursor: pointer;
    display: flex;
    justify-content: flex-end;
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #1483f3;
`;
const TaskDescriptionTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: bold;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    color: #2e2e2e;
`;
const TaskDescriptionText = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: right;
    color: #000;
`;
const TaskIcon = styled_components_browser_esm["b" /* default */].div`
    position: absolute;
    top: 17px;
    left: -48px;
`;
const TaskSelectionBox_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border-left: 1px solid #ccc;
    width: 1px;
    height: 45px;
`;
const TaskSelectionBox_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: ${props => props.width ? props.width : ''};
    height: ${props => props.width ? props.width : ''}; 
`;
// CONCATENATED MODULE: ./src/images/scedual-icon.svg
/* harmony default export */ var scedual_icon = ("js/dist/ac7af81300637e97c12b7244e2b43484.svg");
// CONCATENATED MODULE: ./src/containers/TaskTemplates/TaskSelectionBox/index.tsx











function TaskSelectionBox(props) {
  const {
    task_data
  } = props;
  const getTaskIcon = task_type => {
    switch (task_type) {
      case 'LOAD':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: load,
          width: '61px'
        });
      case 'RESERVE':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: images_reserve,
          width: '61px'
        });
      case 'EXTRACT':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: extract,
          width: '61px'
        });
      case 'LOAD_EXTRACT':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: extract_and_load,
          width: '61px'
        });
      case 'DELETE':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: images_delete,
          width: '61px'
        });
      case 'SYNTHETIC':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
          src: synthetic,
          width: '61px'
        });
    }
  };
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelectionBox_styles_Container, {
    className: "react-comp",
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelectionBox_styles_Header, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskIcon, {
        children: getTaskIcon(task_data.task_type)
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Title, {
        children: task_data.task_title
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelectionBox_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskDescription, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskDescriptionTitle, {
          children: "Task description"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskDescriptionText, {
          children: task_data.task_description
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedSettings, {
        children: "Advanced Settings"
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskBox, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskBoxTitle, {
          children: ["Extract data by Customer from Production ", /*#__PURE__*/Object(jsx_runtime["jsx"])("br", {}), "Subset by Entity list using:"]
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(ExecutionNotes, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(ExecutionNotesTitle, {
            children: "Execution notes"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_TextArea, {
            title: '',
            onChange: () => {}
          })]
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelectionBox_styles_Actions, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
          title: "Previous Executions",
          onClick: () => {},
          type: 'secondary',
          width: '191px',
          height: '45px'
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_TaskActions, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Icon, {
            src: scedual_icon,
            width: '31px'
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelectionBox_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
            title: "Execute Task",
            onClick: () => {},
            width: '120px',
            height: '45px'
          })]
        })]
      })]
    })]
  });
}
/* harmony default export */ var TaskTemplates_TaskSelectionBox = (TaskSelectionBox);
// CONCATENATED MODULE: ./src/containers/TaskTemplates/TaskSelection/index.tsx






function TaskSelection(props) {
  const {
    task_id,
    onBack
  } = props;
  const [taskData, setTaskData] = Object(react["useState"])(null);
  Object(react["useEffect"])(() => {
    async function getTaskData() {
      const result = await apis_task.getTaskById([task_id], 'both');
      if (result && result.length > 0) {
        setTaskData(result[0]);
      }
    }
    getTaskData();
  }, [task_id]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelection_styles_Container, {
    className: "react-comp",
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelection_styles_Header, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskSelection_styles_Title, {
        children: "Execute Task"
      })
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskSelection_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SideContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
          height: '45px',
          title: '< Back',
          onClick: () => onBack(),
          type: 'secondary'
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(MiddleContainer, {
        children: taskData ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_TaskSelectionBox, {
          task_data: taskData
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {})
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(SideContainer, {})]
    })]
  });
}
/* harmony default export */ var TaskTemplates_TaskSelection = (TaskSelection);
// EXTERNAL MODULE: ./node_modules/lucide-react/dist/esm/icons/users.js
var users = __webpack_require__(327);

// EXTERNAL MODULE: ./node_modules/lucide-react/dist/esm/icons/user.js
var user = __webpack_require__(328);

// CONCATENATED MODULE: ./src/components/TaskGroupTabs/styles.ts

const TabContainer = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
  border-radius: 10px;
`;
const Tab = styled_components_browser_esm["b" /* default */].button`
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  padding: 6px 12px;
  font-weight: 500;
  cursor: pointer;
  border-radius: 0px;
  color: ${props => props.active ? "#1483f3" : "#555"} !important;
  background-color: ${props => props.active ? "#fff" : "transparent"} !important;
  border-bottom: ${props => props.active ? "2px solid #1483f3" : "0px"} !important;
`;
const TabIcon = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  svg {
    width: 16px;
    height: 16px;
  }
`;
const TabLabel = styled_components_browser_esm["b" /* default */].span`
  white-space: nowrap;
`;
// CONCATENATED MODULE: ./src/components/TaskGroupTabs/index.tsx




const TaskGroupTabs_tabs = [{
  label: "All groups",
  icon: users["a" /* default */],
  value: 'allTaskGroups'
}, {
  label: "Created by me",
  icon: user["a" /* default */],
  value: 'myTaskGroups'
}];
function TaskGroupTabs(props) {
  const {
    onChange
  } = props;
  const [selectedTab, setSelectedTab] = Object(react["useState"])("allTaskGroups");
  Object(react["useEffect"])(() => {
    onChange(selectedTab);
  }, [selectedTab]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TabContainer, {
    children: TaskGroupTabs_tabs.map(_ref => {
      let {
        label,
        icon: Icon,
        value
      } = _ref;
      return /*#__PURE__*/Object(jsx_runtime["jsxs"])(Tab, {
        active: selectedTab === value,
        onClick: () => setSelectedTab(value),
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TabIcon, {
          as: Icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TabLabel, {
          children: label
        })]
      }, label);
    })
  });
}
// CONCATENATED MODULE: ./src/containers/TaskTemplates/newTaskGroup/styles.ts

const newTaskGroup_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 15px;
    position: relative;
`;
const NewTaskGroupContainer = styled_components_browser_esm["b" /* default */].div`
    width: 400px;
    position: relative;
    z-index: 100;
    max-height: 350px;
    overflow-y: auto;
    overflow-x: hidden;
    padding: 19px 0px 30px 0px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    background-color: #fff;
`;
const newTaskGroup_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.43;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const NewTaskGroupTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 18px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    position: relative;
    margin: 0px 20px;
    margin-bottom: 19px;
`;
const newTaskGroup_styles_Body = styled_components_browser_esm["b" /* default */].div`
    margin: 10px 25px 0px 30px;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const newTaskGroup_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border: solid 1px #ccc;
`;
const newTaskGroup_styles_CloseIcon = styled_components_browser_esm["b" /* default */].img`
    position: absolute;
    right: 0px;
    top: 5px;
    cursor: pointer;
`;
const newTaskGroup_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const newTaskGroup_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const newTaskGroup_styles_ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const styles_SystemHeader = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    padding: 10px 0px;
    border-bottom: 1px solid #ccc;
    justify-content: space-between;
    padding-right: 11px;
`;
const styles_SystemsContainer = styled_components_browser_esm["b" /* default */].div`

`;
const styles_SystemBody = styled_components_browser_esm["b" /* default */].div`
    padding: 13px 10px 15px 37px;
    background-color: #f2f2f2;
    display: flex;
    gap: 12px;
    flex-direction: column;
    border-bottom: solid 1px #ccc;
`;
const styles_LogicalUnitContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    
`;
const styles_ExecutionModeContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    border-bottom: 1px solid #ccc;
    gap: 10px;
    margin-top: 10px;
    padding-bottom: 10px;
`;
// CONCATENATED MODULE: ./src/containers/TaskTemplates/newTaskGroup/index.tsx








function NewTaskGroup(props) {
  var _errors$name2;
  const {
    close,
    data
  } = props;
  const {
    handleSubmit,
    control,
    formState: {
      errors
    }
  } = Object(index_esm["b" /* useForm */])({
    defaultValues: {
      name: (data === null || data === void 0 ? void 0 : data.task_group_name) || '',
      desc: (data === null || data === void 0 ? void 0 : data.task_group_desc) || ''
    }
  });
  const saveData = Object(react["useCallback"])(() => {
    const handle = handleSubmit(formData => {
      async function addTaskGroup() {
        try {
          if (data) {
            await apis_task.updateTaskGroup(data.task_group_id, formData.name, formData.desc);
          } else {
            await apis_task.addTaskGroup(formData.name, formData.desc);
          }
          close(true);
        } catch (err) {
          console.log('unable to add/update task group');
        }
      }
      addTaskGroup();
    });
    handle(); // trigger validation and run callback if valid
  }, [handleSubmit, close, data]);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(NewTaskGroupContainer, {
    onClick: $event => $event.stopPropagation(),
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(NewTaskGroupTitle, {
      children: ["Task Group ", data === null || data === void 0 ? void 0 : data.task_group_name, /*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup_styles_CloseIcon, {
        onClick: () => close(false),
        src: xclose
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(newTaskGroup_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
        name: "name",
        control: control,
        rules: {
          required: "Name is required"
        },
        render: _ref => {
          var _errors$name;
          let {
            field
          } = _ref;
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
            ...field,
            name: "new_task_group_name",
            title: "Name",
            mandatory: true,
            type: InputTypes.text,
            error: (_errors$name = errors.name) === null || _errors$name === void 0 ? void 0 : _errors$name.message
          });
        }
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
        name: "desc",
        control: control,
        render: _ref2 => {
          let {
            field
          } = _ref2;
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
            ...field,
            name: "new_task_group_desc",
            title: "Description",
            mandatory: false,
            type: InputTypes.text
          });
        }
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(newTaskGroup_styles_Actions, {
        border: false,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup_styles_ActionItem, {
          onClick: () => close(false),
          children: "Cancel"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup_styles_ActionItem, {
          onClick: () => saveData(),
          children: "Save"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
        relativePosition: true,
        submit: true,
        error: (_errors$name2 = errors.name) === null || _errors$name2 === void 0 ? void 0 : _errors$name2.message
      })]
    })]
  });
}
/* harmony default export */ var newTaskGroup = (NewTaskGroup);
// CONCATENATED MODULE: ./src/components/DropdownMenu/styles.ts

const MenuWrapper = styled_components_browser_esm["b" /* default */].div`
  position: relative;
  display: inline-block;
`;
const MenuButton = styled_components_browser_esm["b" /* default */].button`
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 4px;
  color: #333;

  &:hover {
    color: #007aff;
  }
`;
const MenuContainer = styled_components_browser_esm["b" /* default */].div`
  position: absolute;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-width: 180px;
  z-index: 10;
`;
const MenuItem = styled_components_browser_esm["b" /* default */].div`
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  color: ${_ref => {
  let {
    $danger
  } = _ref;
  return $danger ? "red" : "#333";
}};

  &:hover {
    background-color: #f5f5f5;
  }
`;
// CONCATENATED MODULE: ./src/components/DropdownMenu/index.tsx



function DropdownMenu(_ref) {
  let {
    items
  } = _ref;
  const [open, setOpen] = Object(react["useState"])(false);
  const menuRef = Object(react["useRef"])(null);
  const handleClickOutside = e => {
    if (menuRef.current && !menuRef.current.contains(e.target)) {
      setOpen(false);
    }
  };
  Object(react["useEffect"])(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(MenuWrapper, {
    ref: menuRef,
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(MenuContainer, {
      children: items.map((item, idx) => /*#__PURE__*/Object(jsx_runtime["jsx"])(MenuItem, {
        onClick: $event => {
          item.onClick($event);
          setOpen(false);
        },
        $danger: item.danger,
        children: item.label
      }, idx))
    })
  });
}
/* harmony default export */ var components_DropdownMenu = (DropdownMenu);
// CONCATENATED MODULE: ./src/containers/TaskTemplates/moveTaskGroup/styles.ts

const moveTaskGroup_styles_Container = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    margin-top: 15px;
    position: relative;
`;
const styles_NewTaskGroupContainer = styled_components_browser_esm["b" /* default */].div`
    width: 400px;
    position: relative;
    z-index: 100;
    max-height: 350px;
    padding: 19px 0px 30px 0px;
    object-fit: contain;
    border-radius: 6px;
    box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.2);
    background-color: #fff;
`;
const moveTaskGroup_styles_Title = styled_components_browser_esm["b" /* default */].span`
    font-family: Roboto;
    font-size: 14px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.43;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const styles_NewTaskGroupTitle = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 18px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    line-height: 1.33;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    position: relative;
    margin: 0px 20px;
    margin-bottom: 19px;
`;
const moveTaskGroup_styles_Body = styled_components_browser_esm["b" /* default */].div`
    margin: 10px 25px 0px 30px;
    display: flex;
    flex-direction: column;
    gap: 10px;
`;
const moveTaskGroup_styles_Seprator = styled_components_browser_esm["b" /* default */].div`
    border: solid 1px #ccc;
`;
const moveTaskGroup_styles_CloseIcon = styled_components_browser_esm["b" /* default */].img`
    position: absolute;
    right: 0px;
    top: 5px;
    cursor: pointer;
`;
const moveTaskGroup_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const moveTaskGroup_styles_Actions = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    margin-top:5px;
    align-items: center;
    justify-content: flex-end;
    gap: 18px;
    border-bottom: ${props => props.border ? '1px solid #ccc' : ''};
    padding-bottom: 13px;
`;
const moveTaskGroup_styles_ActionItem = styled_components_browser_esm["b" /* default */].div`
    font-family: Roboto;
    font-size: 16px;
    font-weight: normal;
    font-stretch: normal;
    font-style: normal;
    letter-spacing: normal;
    text-align: left;
    color: #1483f3;
    cursor: pointer;
`;
const moveTaskGroup_styles_SystemHeader = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    align-items: center;
    padding: 10px 0px;
    border-bottom: 1px solid #ccc;
    justify-content: space-between;
    padding-right: 11px;
`;
const moveTaskGroup_styles_SystemsContainer = styled_components_browser_esm["b" /* default */].div`

`;
const moveTaskGroup_styles_SystemBody = styled_components_browser_esm["b" /* default */].div`
    padding: 13px 10px 15px 37px;
    background-color: #f2f2f2;
    display: flex;
    gap: 12px;
    flex-direction: column;
    border-bottom: solid 1px #ccc;
`;
const moveTaskGroup_styles_LogicalUnitContainer = styled_components_browser_esm["b" /* default */].div`
    width: 100%;
    
`;
const moveTaskGroup_styles_ExecutionModeContainer = styled_components_browser_esm["b" /* default */].div`
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    border-bottom: 1px solid #ccc;
    gap: 10px;
    margin-top: 10px;
    padding-bottom: 10px;
`;
// CONCATENATED MODULE: ./src/containers/TaskTemplates/moveTaskGroup/index.tsx









function MoveTaskGroup(props) {
  var _errors$task_group_id2;
  const {
    close,
    groups,
    task_id,
    from_task_group_id
  } = props;
  const {
    register,
    handleSubmit,
    control,
    formState: {
      errors
    }
  } = Object(index_esm["b" /* useForm */])();
  const saveData = Object(react["useCallback"])(() => {
    const handle = handleSubmit(data => {
      async function assignToTaskGroup() {
        await apis_task.moveTasksToTaskGroup([task_id], from_task_group_id, data.task_group_id.map(it => it.value), data.keepCurrentGroup);
        close(true);
      }
      assignToTaskGroup();
    });
    handle(); // trigger validation and run callback if valid
  }, [handleSubmit, close, task_id, from_task_group_id]);
  const onChange = Object(react["useCallback"])(() => {}, []);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_NewTaskGroupContainer, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_NewTaskGroupTitle, {
      children: ["Task Group", /*#__PURE__*/Object(jsx_runtime["jsx"])(moveTaskGroup_styles_CloseIcon, {
        onClick: () => close(false),
        src: xclose
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(moveTaskGroup_styles_Seprator, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(moveTaskGroup_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
        name: "task_group_id",
        control: control,
        rules: {
          required: "Task group is required"
        },
        render: _ref => {
          var _errors$task_group_id;
          let {
            field
          } = _ref;
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
            ...field,
            width: "290px",
            title: 'Task group',
            mandatory: true,
            value: field.value,
            options: groups,
            isMulti: true,
            onChange: field.onChange,
            error: (_errors$task_group_id = errors.task_group_id) === null || _errors$task_group_id === void 0 ? void 0 : _errors$task_group_id.message
          });
        }
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
        name: "keepCurrentGroup",
        control: control,
        rules: {
          required: "Task group is required"
        },
        render: _ref2 => {
          let {
            field
          } = _ref2;
          return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_checkbox, {
            name: `keep_in_current_group`,
            title: 'Keep task in current group',
            onChange: field.onChange,
            value: field.value
          });
        }
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(moveTaskGroup_styles_Actions, {
        border: false,
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(moveTaskGroup_styles_ActionItem, {
          onClick: () => close(false),
          children: "Cancel"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(moveTaskGroup_styles_ActionItem, {
          onClick: () => saveData(),
          children: "Save"
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(components_FieldError, {
        relativePosition: true,
        submit: true,
        error: (_errors$task_group_id2 = errors.task_group_id) === null || _errors$task_group_id2 === void 0 ? void 0 : _errors$task_group_id2.message
      })]
    })]
  });
}
/* harmony default export */ var moveTaskGroup = (MoveTaskGroup);
// CONCATENATED MODULE: ./src/components/Popup/styles.ts

const Overlay = styled_components_browser_esm["b" /* default */].div`
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
`;
const PopupContainer = styled_components_browser_esm["b" /* default */].div`
  // background-color: white;
  // border-radius: 8px;
  // padding: 24px;
  max-width: ${props => props.width || '600px'};
  width: 100%;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
`;
// CONCATENATED MODULE: ./src/components/Popup/index.tsx



function Popup(_ref) {
  let {
    isOpen,
    onClose,
    children,
    width
  } = _ref;
  if (!isOpen) return null;
  console.log(width);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Overlay, {
    onClick: onClose,
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(PopupContainer, {
      width: width,
      onClick: e => e.stopPropagation(),
      children: children
    })
  });
}
// CONCATENATED MODULE: ./src/containers/TaskTemplates/SearchBar/styles.ts

const SearchContainer = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  border-radius: 10px;
  border: solid 1px #999;
  background-color: #999;
  padding: 0 10px;
  height: 36px;
  width: 33%;
  background: #fff;
  position: absolute;
  left: calc(50% - 16%);
`;
const SearchInput = styled_components_browser_esm["b" /* default */].input`
  border: none;
  flex: 1;
  outline: none;
  font-size: 14px; /* Optional: slightly smaller font to fit nicely */
  color: #000;

  &::placeholder {
    color: #999;
    opacity: 1;
  }
`;
const IconsContainer = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
`;
const Divider = styled_components_browser_esm["b" /* default */].div`
  width: 1px;
  height: 15px;
  background-color: #ccc;
  margin: 0 8px;
`;
const SearchBar_styles_Icon = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
    width: ${props => props.width ? props.width : ''};
    height: ${props => props.height ? props.height : ''}; 
`;
const ModalContainer = styled_components_browser_esm["b" /* default */].div`
  width: 100%;
  background: #fff;
  position: relative;
  display: flex;
  flex-direction: column;
  border-radius: 6px;
  padding: 10px;
  margin-left: -10px;
  box-shadow: 0 0 9px 1px rgba(0, 0, 0, 0.3);
`;
const SearchBar_styles_Header = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  justify-content: flex-end;
`;
const styles_CloseButton = styled_components_browser_esm["b" /* default */].img`
    cursor: pointer;
`;
const SearchBar_styles_Body = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-left: 30px;
  padding-right: 51px;
`;
const FieldRow = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;
const Label = styled_components_browser_esm["b" /* default */].label`
  font-family: Roboto;
  font-size: 16px;
  font-weight: normal;
  font-stretch: normal;
  font-style: normal;
  letter-spacing: normal;
  text-align: left;
  color: #2e2e2e;
`;
const styles_Input = styled_components_browser_esm["b" /* default */].input`
  height: 30px;
  padding: 5px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 70%;
`;
const styles_Select = styled_components_browser_esm["b" /* default */].select`
  height: 30px;
  padding: 5px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  width: 70%;
`;
const AdvancedDivider = styled_components_browser_esm["b" /* default */].div`
  height: 1px;
  background-color: #eee;
  margin: 20px 0;
    margin-left: 30px;
`;
const Footer = styled_components_browser_esm["b" /* default */].div`
  display: flex;
  justify-content: flex-end;
`;
const SearchButton = styled_components_browser_esm["b" /* default */].button`
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 20px;
  font-size: 14px;
  cursor: pointer;

  &:hover {
    background-color: #0069d9;
  }
`;
// CONCATENATED MODULE: ./src/images/search-icon.svg
/* harmony default export */ var search_icon = ("js/dist/fdadb79e6c2d7ebdf1c1506ac400cdc4.svg");
// CONCATENATED MODULE: ./src/images/advanced-menu-icon.svg
/* harmony default export */ var advanced_menu_icon = ("js/dist/6583b8a0f67994cbafec1d25d8f3ecb1.svg");
// CONCATENATED MODULE: ./src/images/x.svg
/* harmony default export */ var x = ("js/dist/210967d56aeda25d75ac1a18a9c8163a.svg");
// CONCATENATED MODULE: ./src/containers/TaskTemplates/SearchBar/AdvancedSearchBar.tsx









const data_types = [{
  label: 'Entities',
  value: 'entities'
}, {
  label: 'Tables',
  value: 'tables'
}, {
  label: 'Entites & Tables',
  value: 'both'
}];
const scheduling_types = [{
  label: 'True',
  value: true
}, {
  label: 'False',
  value: false
}];
const task_types = [{
  value: 'EXTRACT',
  label: 'Extract'
}, {
  value: 'LOAD',
  label: 'Load'
}, {
  value: 'EXTRACT_AND_LOAD',
  label: 'Extract and load'
}, {
  value: 'DELETE',
  label: 'Delete'
}, {
  value: 'RESERVE',
  label: 'Reserve'
}, {
  value: 'SYNTHETIC_GENERATION',
  label: 'Synthetic generation'
}];
const AdvancedSearch = _ref => {
  let {
    onClose
  } = _ref;
  const {
    handleSubmit,
    control
  } = Object(index_esm["b" /* useForm */])();
  const [environments, setEnvironments] = Object(react["useState"])([]);
  const onSubmit = async data => {
    if (data.taskTypesStr && data.taskTypesStr.length > 0) {
      data.taskTypesStr = data.taskTypesStr.map(it => it.value);
    }
    if (data.environmentId) {
      data.environmentId = data.environmentId.value;
    }
    if (data.isScheduled) {
      data.isScheduled = data.isScheduled.value;
    }
    if (data.dataType) {
      data.dataType = data.dataType.value;
    }
    const result = await apis_task.searchTasks(data);
  };
  Object(react["useEffect"])(() => {
    async function fetchEnvironments() {
      const result = await apis_task.getEnvironmentsByUser();
      console.log(result);
      setEnvironments(result.map(it => ({
        label: it.environment_name,
        value: it.environment_id
      })));
    }
    fetchEnvironments();
  }, []);
  console.log(environments);
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(ModalContainer, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SearchBar_styles_Header, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_CloseButton, {
        src: x,
        onClick: onClose
      })
    }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(SearchBar_styles_Body, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Keywords"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "text",
          control: control,
          render: _ref2 => {
            let {
              field
            } = _ref2;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              type: InputTypes.text,
              placeholder: "",
              small: true
            });
          }
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Task type"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "taskTypesStr",
          control: control,
          render: _ref3 => {
            let {
              field
            } = _ref3;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              isMulti: true,
              options: task_types,
              loading: false,
              small: true
            });
          }
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Environment"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "environmentId",
          control: control,
          render: _ref4 => {
            let {
              field
            } = _ref4;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              options: environments // Replace with dynamic env list if needed
              ,
              loading: false,
              small: true
            });
          }
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Creator"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "creator",
          control: control,
          render: _ref5 => {
            let {
              field
            } = _ref5;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Input, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              type: InputTypes.text,
              placeholder: "",
              small: true
            });
          }
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Scheduling"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "isScheduled",
          control: control,
          render: _ref6 => {
            let {
              field
            } = _ref6;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              options: scheduling_types,
              loading: false,
              small: true
            });
          }
        })]
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(FieldRow, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Label, {
          children: "Processed data"
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(index_esm["a" /* Controller */], {
          name: "dataType",
          control: control,
          render: _ref7 => {
            let {
              field
            } = _ref7;
            return /*#__PURE__*/Object(jsx_runtime["jsx"])(Select, {
              ...field,
              width: "70%",
              title: "",
              mandatory: false,
              options: data_types,
              loading: false,
              small: true
            });
          }
        })]
      })]
    }), /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedDivider, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(Footer, {
      children: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_Button, {
        title: "Search",
        onClick: handleSubmit(onSubmit),
        width: "127px",
        height: "40px"
      })
    })]
  });
};
/* harmony default export */ var AdvancedSearchBar = (AdvancedSearch);
// CONCATENATED MODULE: ./src/containers/TaskTemplates/SearchBar/index.tsx







const SearchBar_SearchBar = () => {
  const [open, setOpen] = Object(react["useState"])(false);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
    containerStyle: {
      zIndex: '100',
      width: '33%'
    },
    reposition: true,
    padding: 10,
    align: "center",
    isOpen: open,
    positions: ['bottom'],
    content: /*#__PURE__*/Object(jsx_runtime["jsx"])(AdvancedSearchBar, {
      onClose: () => setOpen(false)
    }),
    children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(SearchContainer, {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SearchInput, {
        placeholder: "Search..."
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(IconsContainer, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(SearchBar_styles_Icon, {
          src: search_icon
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Divider, {}), /*#__PURE__*/Object(jsx_runtime["jsx"])(SearchBar_styles_Icon, {
          onClick: () => setOpen(true),
          src: advanced_menu_icon
        })]
      })]
    })
  });
};
/* harmony default export */ var TaskTemplates_SearchBar = (SearchBar_SearchBar);
// CONCATENATED MODULE: ./src/containers/TaskTemplates/index.tsx
























const TaskTemplates_MySwal = sweetalert2_react_content_umd_default()(sweetalert2_all_default.a);
function TaskTemplates(props) {
  const {
    scope,
    content
  } = props;
  const {
    openTask,
    openTasks,
    openNewTask
  } = content;
  const [searchText, setSearchText] = Object(react["useState"])('');
  const [chosenGroupCategory, setChosenGroupCategory] = Object(react["useState"])('All');
  const [taskGroups, setTaskGroups] = Object(react["useState"])(null);
  const [selectedTab, setSelectedTab] = Object(react["useState"])("allTaskGroups");
  const [selectedTaskGroup, setSelectedTaskGroup] = Object(react["useState"])(null);
  const [open, setOpen] = Object(react["useState"])("");
  const [selectedTaskGroupData, setSelectedTaskGroupData] = Object(react["useState"])([]);
  const [openTaskExecution, setOpenTaskExecution] = Object(react["useState"])(null);
  const chooseCategory = Object(react["useCallback"])(taskGroup => {
    setSelectedTaskGroup(taskGroup);
  }, [setSelectedTaskGroup]);
  const fetchTaskGroups = Object(react["useCallback"])(async () => {
    try {
      const result = await apis_task.getTaskGroups();
      setTaskGroups(result);
      if ((result === null || result === void 0 ? void 0 : result.allTaskGroups.length) > 0 && !selectedTaskGroup) {
        chooseCategory(result.allTaskGroups[0]);
      }
    } catch (err) {}
  }, [setTaskGroups, chooseCategory, selectedTaskGroup]);
  Object(react["useEffect"])(() => {
    fetchTaskGroups();
  }, []);
  const closeNewTaskGroup = Object(react["useCallback"])(status => {
    if (status) {
      fetchTaskGroups();
    }
    setOpen("");
  }, [fetchTaskGroups, setOpen]);
  Object(react["useEffect"])(() => {
    async function fetchTaskGroupById() {
      try {
        const result = await apis_task.getTaskGroupById(selectedTaskGroup === null || selectedTaskGroup === void 0 ? void 0 : selectedTaskGroup.task_group_id);
        setSelectedTaskGroupData(result);
      } catch (err) {}
    }
    fetchTaskGroupById();
  }, [selectedTaskGroup]);
  const toggleTaskGroupFavorite = Object(react["useCallback"])(async (task_group_id, marked) => {
    try {
      await apis_task.toggleTaskGroupFavorite(task_group_id, marked);
      fetchTaskGroups();
    } catch (err) {
      console.error('failed to remove task group;');
    }
  }, [fetchTaskGroups]);
  const toggleTaskFavorite = Object(react["useCallback"])(async (task_id, marked) => {
    try {
      await apis_task.toggleTaskFavorite(task_id, marked);
      fetchTaskGroups();
    } catch (err) {
      console.error('failed to remove task group;');
    }
  }, [fetchTaskGroups]);
  const deleteGroup = Object(react["useCallback"])(async task_group_id => {
    const result = await TaskTemplates_MySwal.fire({
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
        children: "It is recommended to move the related tasks to another task group before deleting this group. Any remaining tasks will be moved to the General group. Do you want to proceed?"
      }),
      showCancelButton: true,
      icon: "warning",
      confirmButtonText: "Yes",
      cancelButtonText: "No"
    }).then(value => {
      return value.isConfirmed;
    }).catch(() => false);
    if (result) {
      try {
        await apis_task.deleteTaskGroup(task_group_id);
        fetchTaskGroups();
      } catch (err) {
        console.error('failed to remove task group;');
      }
    }
  }, [fetchTaskGroups]);
  const getLeftSideTemplates = Object(react["useCallback"])(() => {
    if (!taskGroups) {
      return /*#__PURE__*/Object(jsx_runtime["jsx"])(jsx_runtime["Fragment"], {});
    }
    return taskGroups[selectedTab].map((taskGroup, index) => /*#__PURE__*/Object(jsx_runtime["jsxs"])(LeftSideTemplate, {
      selected: (selectedTaskGroup === null || selectedTaskGroup === void 0 ? void 0 : selectedTaskGroup.task_group_id) == taskGroup.task_group_id,
      onClick: () => chooseCategory(taskGroup),
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(LeftSideTemplateIconContainer, {
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          onClick: $event => {
            $event.stopPropagation();
            toggleTaskGroupFavorite(taskGroup.task_group_id, taskGroup.favorite);
          },
          src: taskGroup.favorite ? selected_favorit : favorite_icon_new
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(LeftSideTemplateName, {
        children: taskGroup.task_group_name
      }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
        containerStyle: {
          zIndex: '100'
        },
        reposition: true,
        padding: -30,
        align: "center",
        isOpen: (open === null || open === void 0 ? void 0 : open.indexOf(`group_list_${taskGroup.task_group_id}`)) >= 0,
        positions: [open === `edit_group_list_${taskGroup.task_group_id}` ? 'right' : 'bottom'],
        transformMode: "relative",
        onClickOutside: () => {
          closeNewTaskGroup(false);
        },
        content: open === `edit_group_list_${taskGroup.task_group_id}` ? /*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup, {
          data: taskGroup,
          close: closeNewTaskGroup
        }) : /*#__PURE__*/Object(jsx_runtime["jsx"])(components_DropdownMenu, {
          items: [{
            label: "Edit Group",
            onClick: $event => {
              setOpen(`edit_group_list_${taskGroup.task_group_id}`);
              $event.stopPropagation();
            }
          }, {
            label: "Delete Group",
            onClick: $event => {
              deleteGroup(taskGroup.task_group_id);
              $event.stopPropagation();
            }
          }]
        }),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(DotMenuIconWrapper, {
          onClick: $event => {
            setOpen(`group_list_${taskGroup.task_group_id}`);
            $event.stopPropagation();
          },
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
            width: '13px',
            height: '3px',
            src: dotmenu
          })
        })
      }, `menu_task_group_${index}`)]
    }, `task_group_${index}`));
  }, [taskGroups, selectedTaskGroup, chooseCategory, selectedTab, setOpen, deleteGroup, closeNewTaskGroup, open]);
  const getRightSideBoxIcon = task_type => {
    switch (task_type.toUpperCase()) {
      case 'EXTRACT AND LOAD':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: load,
          width: '47px'
        });
      case 'LOAD':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: load,
          width: '47px'
        });
      case 'RESERVE':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: images_reserve,
          width: '47px'
        });
      case 'EXTRACT':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: extract,
          width: '47px'
        });
      case 'LOAD_EXTRACT':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: extract_and_load,
          width: '47px'
        });
      case 'DELETE':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: images_delete,
          width: '47px'
        });
      case 'SYNTHETIC':
        return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
          src: synthetic,
          width: '47px'
        });
    }
  };
  const getAndOpenTask = Object(react["useCallback"])(async (task_id, copy) => {
    const result = await apis_task.getTaskById([task_id], 'Active');
    if ((result === null || result === void 0 ? void 0 : result.length) > 0) {
      openTask(result[0], copy, []);
    }
  }, [openTask]);
  const deleteTask = Object(react["useCallback"])(async (task_id, task_title) => {
    const result = await TaskTemplates_MySwal.fire({
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
        children: "You are about to permanently delete this task. The task will no longer be available. Are you sure you want to proceed?"
      }),
      showCancelButton: true,
      icon: "warning",
      confirmButtonText: "Yes",
      cancelButtonText: "No"
    }).then(value => {
      return value.isConfirmed;
    }).catch(() => false);
    if (result) {
      try {
        await apis_task.deleteTask(task_id, task_title);
        if (selectedTaskGroup) {
          setSelectedTaskGroup({
            ...selectedTaskGroup
          });
        }
      } catch (err) {
        console.error(err);
      }
    }
  }, [setSelectedTaskGroup, selectedTaskGroup]);
  const deleteTaskFromGroup = Object(react["useCallback"])(async (task_id, task_group_id) => {
    if (!task_id || !task_group_id) {
      return;
    }
    const result = await TaskTemplates_MySwal.fire({
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
        children: "This task will be moved to the General group if it's not attached to another group. Are you sure you want to remove it from this group?"
      }),
      showCancelButton: true,
      icon: "warning",
      confirmButtonText: "Yes",
      cancelButtonText: "No"
    }).then(value => {
      return value.isConfirmed;
    }).catch(() => false);
    if (result) {
      try {
        await apis_task.deleteTaskFromGroup(task_id, task_group_id);
        if (selectedTaskGroup) {
          setSelectedTaskGroup({
            ...selectedTaskGroup
          });
        }
      } catch (err) {
        console.error(err);
      }
    }
  }, [setSelectedTaskGroup, selectedTaskGroup]);
  const getRightSideTemplates = Object(react["useCallback"])(() => {
    return selectedTaskGroupData.map(taskTemplate => /*#__PURE__*/Object(jsx_runtime["jsxs"])(RightSideBox, {
      onClick: () => setOpenTaskExecution(taskTemplate.task_id),
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RightSideBoxHeader, {
        children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(RightSideBoxHeaderActions, {
          onClick: $event => $event.stopPropagation(),
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
            onClick: $event => {
              $event.stopPropagation();
              toggleTaskFavorite(taskTemplate.task_id, taskTemplate.favorite);
            },
            src: taskTemplate.favorite ? selected_favorit : favorite_icon_new,
            width: '17px'
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
            containerStyle: {
              zIndex: '100'
            },
            reposition: true,
            padding: 0,
            align: "center",
            isOpen: open === `menu_task_${taskTemplate.task_id}`,
            positions: ['bottom', 'left'],
            onClickOutside: () => {
              closeNewTaskGroup(false);
            },
            content: /*#__PURE__*/Object(jsx_runtime["jsx"])(components_DropdownMenu, {
              items: [{
                label: "Edit task",
                onClick: () => getAndOpenTask(taskTemplate.task_id, false)
              }, {
                label: "Save as",
                onClick: () => getAndOpenTask(taskTemplate.task_id, true)
              }, {
                label: "Move to group",
                onClick: () => setOpen(`movetocategory_${taskTemplate.task_id}_${selectedTaskGroup === null || selectedTaskGroup === void 0 ? void 0 : selectedTaskGroup.task_group_id}`)
              }, {
                label: "Delete from group",
                onClick: () => deleteTaskFromGroup(taskTemplate.task_id, selectedTaskGroup === null || selectedTaskGroup === void 0 ? void 0 : selectedTaskGroup.task_group_id)
              },
              //   { label: "Disable execution", onClick: () => console.log("Hold Execution") },
              {
                label: "Delete task",
                onClick: () => deleteTask(taskTemplate.task_id, taskTemplate.task_title)
              }]
            }),
            children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
              src: dotmenu,
              width: "13px",
              height: "3px",
              onClick: $event => {
                $event.stopPropagation();
                setOpen(prev => prev === `menu_task_${taskTemplate.task_id}` ? '' : `menu_task_${taskTemplate.task_id}`);
              }
            })
          })]
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(RightSideBoxBody, {
        children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTypeIconContainer, {
          children: getRightSideBoxIcon(taskTemplate.display_task_type)
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(RightSideBoxBodyTitle, {
          title: taskTemplate.task_title,
          children: taskTemplate.task_title
        }), /*#__PURE__*/Object(jsx_runtime["jsx"])(StartIconContainer, {
          children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Icon, {
            title: 'Execute task',
            src: open_execution,
            width: '15px'
          })
        })]
      })]
    }, `task_${taskTemplate.task_id}`));
  }, [selectedTaskGroupData, setOpen, open, selectedTaskGroup, setOpenTaskExecution]);
  const onBack = Object(react["useCallback"])(() => {
    setOpenTaskExecution(null);
    setOpen('');
  }, [setOpenTaskExecution]);
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Container, {
    className: "react-comp",
    children: openTaskExecution ? /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_TaskSelection, {
      task_id: openTaskExecution,
      onBack: onBack
    }) : /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
      children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(Popup, {
        width: '350px',
        isOpen: (open === null || open === void 0 ? void 0 : open.indexOf('movetocategory_')) === 0,
        onClose: () => closeNewTaskGroup(false),
        children: /*#__PURE__*/Object(jsx_runtime["jsx"])(moveTaskGroup, {
          task_id: parseInt(open === null || open === void 0 ? void 0 : open.split('_')[1]),
          from_task_group_id: parseInt(open === null || open === void 0 ? void 0 : open.split('_')[2]),
          close: closeNewTaskGroup,
          groups: ((taskGroups === null || taskGroups === void 0 ? void 0 : taskGroups.allTaskGroups) || []).map(task_group => ({
            label: task_group.task_group_name,
            value: task_group.task_group_id
          }))
        })
      }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(jsx_runtime["Fragment"], {
        children: [/*#__PURE__*/Object(jsx_runtime["jsxs"])(styles_Header, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_Title, {
            children: "Manage Your Tasks"
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_SearchBar, {}), /*#__PURE__*/Object(jsx_runtime["jsxs"])(HeaderActions, {
            children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TaskActionContainer, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskAction, {
                onClick: () => openNewTask([], false),
                children: "New task"
              })
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TaskActionContainer, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Popover["Popover"], {
                containerStyle: {
                  zIndex: '100'
                },
                reposition: true,
                padding: 35,
                align: "center",
                isOpen: open === 'new_task_group',
                positions: ['bottom'],
                transformMode: "relative",
                onClickOutside: () => {
                  closeNewTaskGroup(false);
                },
                content: /*#__PURE__*/Object(jsx_runtime["jsx"])(newTaskGroup, {
                  close: closeNewTaskGroup
                }),
                children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskAction, {
                  onClick: () => setOpen('new_task_group'),
                  children: "New group"
                })
              })
            }), /*#__PURE__*/Object(jsx_runtime["jsx"])(styles_TaskActionContainer, {
              children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskAction, {
                children: "All task executions"
              })
            })]
          })]
        }), /*#__PURE__*/Object(jsx_runtime["jsxs"])(TaskTemplates_styles_Body, {
          children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_LeftSide, {
            children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(CategoriesContainer, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(LeftSideHeader, {
                children: "Task groups"
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(LeftSideFilter, {
                children: /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskGroupTabs, {
                  onChange: setSelectedTab
                })
              }), getLeftSideTemplates()]
            })
          }), /*#__PURE__*/Object(jsx_runtime["jsx"])(TaskTemplates_styles_RightSide, {
            children: /*#__PURE__*/Object(jsx_runtime["jsxs"])(RightSideBody, {
              children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(RightSideHeader, {
                children: selectedTaskGroup === null || selectedTaskGroup === void 0 ? void 0 : selectedTaskGroup.task_group_name
              }), /*#__PURE__*/Object(jsx_runtime["jsx"])(BoxesContainer, {
                children: getRightSideTemplates()
              })]
            })
          })]
        })]
      })]
    })
  });
}
/* harmony default export */ var containers_TaskTemplates = (TaskTemplates);
// CONCATENATED MODULE: ./src/utils/ExportToNg.jsx





const TaskMainComp = props => {
  return /*#__PURE__*/Object(jsx_runtime["jsx"])(react_default.a.StrictMode, {
    children: /*#__PURE__*/Object(jsx_runtime["jsx"])(Main, {
      content: props.content
    })
  });
};
react_to_angular(TaskMainComp, "reactTaskMain",
// eslint-disable-next-line no-undef
angular.module("react-connector"), {
  content: '='
});
const TaskTemplatesComp = props => {
  return /*#__PURE__*/Object(jsx_runtime["jsxs"])(react_default.a.StrictMode, {
    children: [/*#__PURE__*/Object(jsx_runtime["jsx"])(containers_TaskTemplates, {
      content: props.content
    }), ";"]
  });
};
react_to_angular(TaskTemplatesComp, "reactTaskTemplates",
// eslint-disable-next-line no-undef
angular.module("react-connector"), {
  content: '='
});

/***/ })

/******/ });
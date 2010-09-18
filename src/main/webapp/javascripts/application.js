$(document).ready(function() {
   bindCSS();
   //$('#stdin').attr('placeholder', '> ');
   //buildMethodView([['public static void main', 2], ['public method yay', 6]]);
});

var F_EDITOR_PANE = "frame_editorpane";

/**
 * Can only be called if document is ready
 */
function withEditorPaneWindow(fcn) {
  if (window.frames[F_EDITOR_PANE] != null) {
    fcn(window.frames[F_EDITOR_PANE]);
  } else {
    // HACK: wait invoke in a setTimeout function now, hoping that it will
    // load later
    setTimeout(function() {
      //console.log("Had to load later");
      fcn(window.frames[F_EDITOR_PANE]);
    }, 3000);
  }
}

function bindCSS(){
  var cssLink = document.createElement("link");
  cssLink.href = "style.css";
  cssLink.rel = "stylesheet";
  cssLink.type = "text/css";
  
  var jquery = document.createElement("script");
  jquery.src = "/javascripts/jquery-1.4.2.min.js"
  jquery.type = "text/javascript";

  withEditorPaneWindow(function (win) {
    win.document.body.appendChild(cssLink);
    //win.document.body.appendChild(jquery);
  });
  //console.log("Bound CSS and JS to iframe");
  
}

/**
 * Clear all lines of errors in the editor pane.
 */
function clearLines() {
  elements = getElementsByClass('line-error', window.frames["frame_editorpane"].document, 'img')
  for (i = 0; i < elements.length; i++){
    elements[i].parentNode.removeChild(elements[i])
  }
}

function highlightLine(line, error){
  withEditorPaneWindow(function (win) {
    win.children[0].children["line_" + line].innerHTML = "<img class='line-error' src='images/cancel.png' title='" + error + "' />" + line;
  });
}

function highlightWarningLine(line, error){
  withEditorPaneWindow(function (win) {
    win.children[0].children["line_" + line].innerHTML = "<img class='line-error' src='images/exclamation.png' title='" + error + "' />" + line;
  });
}

function getElementsByClass(searchClass,node,tag) {
	var classElements = new Array();
	if ( node == null )
		node = document;
	if ( tag == null )
		tag = '*';
	var els = node.getElementsByTagName(tag);
	var elsLen = els.length;
	for (i = 0, j = 0; i < elsLen; i++) {
		if ( searchClass == els[i].className ) {
			classElements[j] = els[i];
			j++;
		}
	}
	return classElements;
}

function buildMethodView(list){
  //console.log("buildMethodView called: " + list)
  var ul_container = $('#method-list ul')
  for(var i = 0; i < list.length; i++){
    var method_name = list[i][0];
    var method_line = list[i][1];
    //console.log("method name: " + method_name);
    var list_element = $("<li>" + method_name +"</li>").attr("title", method_line)
    ul_container.append(list_element);
    list_element.click(function(){
      jumpToMethod(this);
    });
  }
}

function addMethodView(method_name, method_line) {
  var ul_container = $('#method-list ul')
  var list_element = $("<li>" + method_name +"</li>").attr("title", method_line)
  ul_container.append(list_element);
  list_element.click(function(){
    jumpToMethod(this);
  });
}

function clearMethodView() {
  $("#method-list ul li").remove();
}

function jumpToMethod(ele){
  editAreaLoader.execCommand('editorpane', 'go_to_line', ele.title);
  return false;
}

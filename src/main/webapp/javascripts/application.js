$(document).ready(function() {
   bindCSS();
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
  withEditorPaneWindow(function (win) {
    win.document.body.appendChild(cssLink);
  });
}

/**
 * Clear all lines of errors in the editor pane.
 */
function clearLines() {
  // TODO: Amber/Allen - implement me!
}

function highlightLine(line, error){
  console.log(error)
  withEditorPaneWindow(function (win) {
    win.children[0].children["line_" + line].innerHTML = "<img src='images/exclamation.png' title='" + error + "' />" + line;
  });
}

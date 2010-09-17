function highlightLine(line, error){
  console.log(error)
  window.frames["frame_editorpane"].children[0].children["line_" + line].innerHTML = "<img src='images/exclamation.png' title='" + error + "' />" + line;
}
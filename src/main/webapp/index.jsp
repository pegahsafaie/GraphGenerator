<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Distant Reading visualization</title>
  <link href="css/index.css" rel="stylesheet" type="text/css" />
  <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
  <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
  <script src="https://www.w3schools.com/lib/w3.js"></script>
  <script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>
<script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=Promise,fetch"></script>
<script type="text/javascript" src="js/jquery-1.12.3.min.js"></script>
<script src="js/jquery-ui.min.js"></script>    
 
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/highcharts-more.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>

<script src="https://cdn.polyfill.io/v2/polyfill.min.js?features=Promise,fetch"></script>
<script type="text/javascript" src="js/jquery-1.12.3.min.js"></script>
<script src="js/jquery-ui.min.js"></script>

<!-- ----------------------- -->
<link href="css/cytoscape.js-panzoom.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css"
	href="http://cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.css">

	<script
		src="https://cdn.polyfill.io/v2/polyfill.min.js?features=Promise,fetch"></script>
	<script src="https://unpkg.com/cytoscape/dist/cytoscape.min.js"></script>
	<script src="http://code.jquery.com/jquery-2.0.3.min.js"></script>
	<script type="text/javascript" src="js/jquery-ui.min.js"></script>
<script src="js/cytoscape-panzoom.js"></script>
	<script type="text/javascript" src="js/cytoscape-qtip.js"></script>
	<script
		src="http://cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.js"></script>
	<script src="js/functionality.js"></script>
	<link href="css/resultViewer.css" rel="stylesheet" />
	
</head>
<body>
 
<div class="container">

	<ul class="tabs">
		<li class="tab-link current" data-tab="tab-1" id="tab1_id">Upload</li>
		<li class="tab-link" data-tab="tab-2">Character and Relations</li>
		<li class="tab-link" data-tab="tab-3">Distribution</li>
		<li class="tab-link" data-tab="tab-4">Timeline</li>
		<li class="tab-link" data-tab="tab-5">Query</li>
		<li class="tab-link" data-tab="tab-6">Event</li>
		<li class="tab-link" data-tab="tab-7">Export</li>
		<li class="tab-link" data-tab="tab-8" id="tab8_id">Evaluation</li>
	</ul>

	<div id="tab-1" class='tab-content current'>
		It is an visualization based on our distant learning system.
		you can use our analyzed sample to see the generated charts, or
		upload your story file into our system to get analyzed JSON result and
		visualization. If you want to upload your story file, please enter each chapter
		into a new paragraph.
		<br/>
		
		<form action="upload" method="post" enctype="multipart/form-data">
		 <select id="selectMode">
		  <option value="story">Story</option>
		  <option value="SampleStoryArthur">Sample Story Arthur King</option>
		  <option value="story_coref">Sample Story Arthur King coref</option>
		  <option value="SampleStoryRobinhood">Sample Story Robinhood</option>
		  <option value="SampleInterview">Sample Interview-remove character mode</option>
		  <option value="SampleInterview_all">Sample Interview-all character mode</option>
		  <option value="Socrates">Socrates Biography</option>
		  <option value="Socrates_coref">Socrates Biography coref</option>
		  <option value="SampleArticle">Iran 2009 protests</option>
		  <option value="article_coref">Iran 2009 protests coref</option>
		  <option value="Shahnameh">Shahnameh</option>
		  <option value="shahnameh_coref">Shahnameh coref</option>
		</select> 
		
		<input type="file" name="file" id="btn-file" />
		<% Object s = request.getAttribute("success"); %>
		<% Object f = request.getAttribute("fail"); %>
	    <% if (s!=null) { %> 
	    	<br/>
			<p>the information extraction pipeline is finished.</p>
	   <% } else if(f!=null){ %>
	   		<br/>
	   		<p>there was a problem during analyzing text. please check you input and your app setting.</p>
	    <% } %>
		<%= ((request.getAttribute("message") == null) ? "" :  request.getAttribute("message")) %>
		<br/>
		<input name="chk-coreferenceWithPyCobalt" type="checkbox" >Co-reference resolving by Py-Cobalt</input>
		<br/>
		<input name="chk-coreferenceWithCoreNLP" type="checkbox" checked="checked">Co-reference resolving by CoreNLP(slow)</input>
		<br/>
		<input name="chk-useQuote" type="checkbox" checked="checked">use quotes in profile extractions</input>
		<br/>
		<input name="chk-removeProfilesWitoutInfo" type="checkbox" checked="checked">remove characters without adjective and verb</input>
		<br/>
		<input name="chk-removeProfilesWithFreq1" type="checkbox">remove characters with frequency 1</input>
		<br/>
		<input name="chk-removeEventsWithNoCharacterObject" type="checkbox">remove events without character object</input>
		<br/>
		<input name="chk-removeEventsWithoutLocationAndObejct" type="checkbox" checked="checked">remove events without object and location</input>
		<br/>
		<input name="chk-useCoreNLPToExtractEvent" type="checkbox" checked="checked">use CoreNLP instead of defined hand rules to extract quotes(slow)</input>
		<br/>
		<!-- <input id="generate_btn" type="button" value="Generate"></input> --> 
		<input id="upload_btn" type="submit" value="Generate" disabled="disabled"></input>
		</form>
	</div>
	<div id="tab-2" class="tab-content">
		<input id="btnRenderGraph" type="button" data-toggle="tooltip" title="Use this button just first time for rendering the graph" value="render Graph"></input>
			<div id="cy"></div>		   
	</div>
	<div id="tab-6" class="tab-content">
		<input id="btnRenderEvent" type="button" data-toggle="tooltip" title="Use this button just first time for rendering the graph" value="render Graph"></input>
			<div id="cy-event"></div>		   
	</div>
	<div id="tab-3" class="tab-content">
		<div id="distributionContainer" style="min-width: 310px; max-width: 800px; height: 400px; margin: 0 auto"></div>
	</div>
	<div id="tab-4" class="tab-content">
		<div id="Ganttcontainer" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
	</div>
	<div id="tab-5" class="tab-content">
	<input id="btnRenderWholeGraph" type="button" data-toggle="tooltip" title="Use this button just first time for rendering the graph" value="render Graph"></input>
	<input id="btnCustomQueryGraph" type="button" value="Custom Search Graph"></input>
	<input id="btnFilterGraph" type="button" data-toggle="tooltip" title="Use this button to show/hide the elements. it is also useful when you want to reset the graph to its main view" value="Filter Graph"></input>
	<br/>
	
	<input id="input_CustomSearchText" type="text" placeholder="Show Characters with"></input>
	<input id="input_location" type="checkbox" name="filter" value="Location" checked > location</input> 
	<input id="input_Time" type="checkbox" name="filter" value="Time" checked > time</input>
	<input id="input_adj" type="checkbox" name="filter" value="adj" checked >adjective</input>
	<input id="input_Verb" type="checkbox" name="filter" value="Verb" checked > verb</input>
	<input id="input_personality" type="checkbox" name="filter" value="personality" checked />personality</input>
		<div id="cyCustom"></div>
	</div>
	<div id="tab-7" class="tab-content" >
		<select id="selectChart">
		  <option value="event">Event graph</option>
		  <option value="character">Character graph</option>
		</select>
		<input id="btnExport" type="button" value="Export"></input>
		<div style="height: 500px; overflow:scroll">
		<img src="" id="imgExport" style="width:2000px;height:2000px"/>
		</div>		   
	</div>
	
	<div id="tab-8" class="tab-content" >
	<form action="upload" method="post" enctype="multipart/form-data">
	here you can see the evaluate results for our sample texts. or choose the story in the drop down and 
	then upload your files.
		<br/>
		<select name="evaluation_selectMode">
		  <option value="story">Story</option>
		  <option value="SampleStoryArthur">Sample Story Arthur King</option>
		  <option value="Socrates">Socrates Biography</option>
		  <option value="SampleInterview">Interview with Trump</option>
		  <option value="SampleArticle">Article</option>
		  
		  <option value="story_coref">Sample Story Arthur King coref</option>
		  <option value="Socrates_coref">Socrates Biography coref</option>
		  <option value="SampleInterview_coref">Interview with Trump coref</option>
		  <option value="article_coref">Article coref</option>
		</select> 
		<br/>
	   <label>Generated Profile File</label><input type="file" name="generatedProfileFile" /><br/>
	   <label>Generated Relation File</label><input type="file" name="generatedRelationFile" /><br/>
	   <label>Annotated ProfileFile</label><input type="file" name="annotatedProfileFile" /><br/>
	   <label>Annotated Relation File</label><input type="file" name="annotatedRelationFile" /><br/>
	   <input id="upload_btn" type="submit" value="Evaluate"></input>
	   <br/>
	    <textarea id="eval_txt" rows="10" cols="120"><%= request.getAttribute("foo") %></textarea>
	   </form>
	</div>

</div><!-- container -->
 
 
</body>
</html>
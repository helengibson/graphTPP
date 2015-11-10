# graphTPP

Licenced under GNU GPL v2

This is the code repository for graphTPP. The original TPP program developed by Joe Faith can be found here 
https://code.google.com/p/targeted-projection-pursuit/


Copyright (C) 2015  Helen Gibson

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

# Intro to graphTPP
graphTPP is a graph layout method and software that emphasises using attributes and clustering for layout. 

Data from the applied soft computing paper can be found under data/AppliedSoftComputing

Details of the commands run in R to create the data sets can be found in data/CreateGraphs.R

To load the attribute data select File > Load data from ARFF file
And select one of the smallworld ARFF files

To load the edge data select 'Graph Options' from the right hand panel and the click 'Load Graph'

*Leave the node identifier as the 'Id' field
*Select one of the edges file
*Tick 'Ignore header'
*Leave use edge weights unticked
*Leave the delimiter as a comma
*Click OK

To interact with the graph nodes can be selected with by dragging a rubber band selection or a whole cluster can be selected by clicking its name in the top panel

Separation of clusters can run automatically by pressing and holding the 'Separate Points' button

#Importing personal graph files

To run your own files in graphTPP requires their import and formatting in Weka first. 
http://www.cs.waikato.ac.nz/ml/weka/

Once Weka is running import your csv file with the node attributes. 

The file must roughly have the format

Node attributes | Classes | NodeId

Node attributes must have the numeric format (try the normalize procedure if you have attributes on lots of different scales)

Classes have the nominal format

NodeId has the string format. 

This can then be exported as a ARFF file to be imported into graphTPP

The edgelist has the format
source,target 

as a csv file where the source and target columns reference the nodeid from the ARFF file. 



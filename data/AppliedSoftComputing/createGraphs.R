library(igraph)

# Usage
# 
# watts.strogatz.game(dim, size, nei, p, loops = FALSE, multiple = FALSE)
# Arguments
# 
# dim  
# Integer constant, the dimension of the starting lattice.
# 
# size  
# Integer constant, the size of the lattice along each dimension.
# 
# nei  
# Integer constant, the neighborhood within which the vertices of the lattice will be connected.
# 
# p  
# Real constant between zero and one, the rewiring probability.
# 
# loops  
# Logical scalar, whether loops edges are allowed in the generated graph.
# 
# multiple	
# Logical scalar, whether multiple edges are allowed int the generated graph.

# Create the graph 
G <- watts.strogatz.game(1, 2000, 2, 0.05)
vcount(G)
ecount(G)

### Community Detection tests

memberships <- list()

### edge.betweenness.community
ebc <- edge.betweenness.community(G)
membership(ebc)
memberships$`Edge betweenness` <- membership(ebc)

### fastgreedy.community
fc <- fastgreedy.community(G)
membership(fc)
memberships$`Fast Greedy` <- membership(fc)

### leading.eigenvector.community
lec <- leading.eigenvector.community(G)
membership(lec)
memberships$`Leading eigenvector` <- membership(lec)

### spinglass.community
sc <- spinglass.community(G, spins=10)
membership(sc)
memberships$`Spinglass` <- membership(sc)

### walktrap.community
wt <- walktrap.community(G, modularity=TRUE)
membership(wt)
memberships$`Walktrap` <- membership(wt)

### label.propagation.community
lpc <- label.propagation.community(G)
membership(lpc)
memberships$`Label propagation` <- membership(lpc)

# get data frame of communities
df <- data.frame(matrix(unlist(memberships), nrow=vcount(G)))
names(df) <- c("ebc", "fg", "lev", "sp", "wt", "lp")
df$Id <- c(1:vcount(G))

# get the adjacency matrix
adj <- get.adjacency(G, type="both")
adj.df <- as.data.frame(as.matrix(adj)) 

# join the adjacency matrix and the communities into one attributes file
smallworld <- cbind(adj.df, df)

# write the file to be used to  create the ARFF attributes file
write.csv(smallworld, file="smallworld.atts.csv", row.names=F)

# write the edgelist to be used with gTPP and Gephi
edge <- get.edgelist(G)
write.csv(edge, file="smallworld.edges.csv", row.names=F)

# write the nodelist that can be imported into Gephi
write.csv(df, file="smallworld.nodes.gephi.csv", row.names=F)
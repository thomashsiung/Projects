import networkx as nx
from networkx.algorithms.approximation import min_weighted_dominating_set
from parse import read_input_file, write_output_file, read_output_file
from utils import is_valid_network, average_pairwise_distance
import sys

def solve(G):
    """
    Args:
        G: networkx.Graph
    Returns:
        T: networkx.Graph
    """

    MSTgraph = nx.Graph()
    special = chkspecialcase(G)
    if special:
        MSTgraph.add_node(special)
        return MSTgraph

    return pruning(nx.minimum_spanning_tree(G))

    """
    MSTcost = average_pairwise_distance(MSTgraph)

    Astargraph = astarsolve(G)
    try:
        Astarcost = average_pairwise_distance(Astargraph)
    except:
        return MSTgraph

    if MSTcost <= Astarcost:
        return MSTgraph
    return Astargraph
    """

#Checks for supernode that connects all, if so, just output that
def chkspecialcase(G):
    supernode_degree = G.number_of_nodes() - 1
    for vertex in G.nodes():
        if len(G[vertex]) == supernode_degree:
            return vertex
    return False

# ALT solve: Uses built-in A* pathing to connect ranked list (by neighbor count)
def astarsolve(G):
    domset = min_weighted_dominating_set(G)
    ranklist = [i for i in domset]
    ranklist.sort(reverse=True, key=lambda x: len(G[x]))
    graph3 = nx.Graph()

    # Heuristic for A*, uses avg MST cost
    MSTcost = average_pairwise_distance(pruning(nx.minimum_spanning_tree(G)))
    def heur(a, b):
        return MSTcost

    k = 1
    while k < len(ranklist):
        a = nx.astar_path(G, ranklist[0], ranklist[k], heur, G[0][k]['weight'])
        for i in range(len(a) - 1):
            graph3.add_weighted_edges_from([(a[i], a[i+1], G[a[i]][a[i+1]]['weight'])])
        k += 1
    return graph3


# prunes leaves of MST
def pruning(Gr):
    vertexcounter = {}
    for edge in Gr.edges():
        (v1,v2) =  (edge[0],edge[1])
        if v1 in vertexcounter :
            vertexcounter[v1]+=1
        else:
            vertexcounter[v1] = 1
        if v2 in vertexcounter :
            vertexcounter[v2]+=1
        else:
            vertexcounter[v2] = 1

    for vertexcount in vertexcounter:
        if vertexcounter[vertexcount] == 1:
            Gr.remove_node(vertexcount)
    return Gr

#pass

# Here's an example of how to run your solver.

# Usage: python3 solver.py test.in

if __name__ == '__main__':
    assert len(sys.argv) == 2
    path = sys.argv[1]
    G = read_input_file(path)
    T = solve(G)
    #print(read_output_file(path, G))
    assert is_valid_network(G, T)

    print("Average pairwise distance: {}".format(average_pairwise_distance(T)))
    write_output_file(T, 'output.out')
    #print(read_output_file('output.out', G))

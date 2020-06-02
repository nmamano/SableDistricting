import math
from collections import deque


# To see that why this code is giving results different from that of the DIMACS competition data, take a look at the
#   Rhode Island data.  In our .txt file for Rhode Island, there is a point at -71793226, 42004865, but there is no such
#   point in the DIMACS file.  In order to see which file is correct, check out the TIGER/Line file given by the
#   Census Bureau for the 2000 data located at ftp://ftp2.census.gov/geo/tiger/tigerua/RI/.  The point is in the file
#   TGR44007.zip and within the zip file, it can be found in the file TGR4400.zip.  As can be verified by the Census
#   Bureau data, the point is in the text file created by this program but not in the DIMACS competition data.


class Node:
    def __init__(self, number, longitude, latitude):
        self.number = number
        self.longitude = longitude
        self.latitude = latitude
        self.label = ""
        self.lex_min_start = {}  # stores all of the vertices in the neighborhood of this Node that start the beginning of a minimum lexicographic ordering
        self.nbd_order = None
        self.degree = -1
        self.special_name = None  # this string is specifically used for labeling a seed vertex while drawing a graph
        self.matched = False  # used to indicate if this Node has been mapped to a vertex in the other graph
        self.matched_number = -1  # this integer tells us what vertex this Node was mapped to in the other graph if matched == True
        self.draw_matched_number = False  # this boolean will tell us whether or not to draw the matched number (if all are drawn, there are too many labels)
        self.matched_id = -1  # this integer will allow us to show which vertices were mapped together when drawing
        self.bfs_comp = -1
        self.draw_green = False
        self.removed_as_bad_seed = False

        self.draw_yellow_label = False

    def copy(self):
        n = Node(self.number, self.longitude, self.latitude)
        n.label = self.label
        n.lex_min_start = {x : self.lex_min_start[x] for x in self.lex_min_start.keys()}
        n.nbd_order = None if self.nbd_order is None else [x for x in self.nbd_order]
        n.degree = self.degree
        n.special_name = self.special_name
        n.matched = self.matched
        n.matched_number = self.matched_number
        n.draw_matched_number = self.draw_matched_number
        n.matched_id = self.matched_id
        n.bfs_comp = self.bfs_comp
        n.draw_green = self.draw_green
        n.removed_as_bad_seed = self.removed_as_bad_seed

        return n

    def __eq__(self, other):
        return True if self.number == other.number else False

    def __hash__(self):
        return hash(self.number)

    def __str__(self):
        return "name-" + str(self.number) + "  x: " + str(self.longitude) + " y: " + str(self.latitude)

class Graph:
    def __init__(self, file=None):
        """
        If file is None, then an empty graph is created.  This is useful when calling the function
        get_connected_subgraph.  If file is not None, then the file is expected to be from
        http://www.dis.uniroma1.it/challenge9/data/tiger/.  It reads all of the nodes first and stores those in
        self.nodes.  The latitude and longitude data are stored with the node in order to know where to draw the nodes
        and edges.  Then it reads all of the edges and stores them into an adjacency list.
        Parameters
        ----------
        file : str
            if not None, then the file should be of a road network from the website listed above

        Returns
        -------
        """
        if file is None:
            self.edges = {}
            self.nodes = {}
            self.num_nodes = 0
            self.num_edges = 0
        else:
            self.edges = {}
            self.nodes = {}

            f = open(file, 'r')

            split_line = f.readline().split()

            self.num_nodes = int(split_line[0])

            for i, line in enumerate(f):
                if i == self.num_nodes:
                    self.num_edges = int(split_line[0])
                    break

                nodeLongLat = line.split()
                node_name = int(nodeLongLat[0])
                longi, lat = float(nodeLongLat[1]), float(nodeLongLat[2])

                n = Node(node_name, longi, lat)

                self.nodes[n.number] = n

            count = 0
            for line in f:
                edges = line.split()

                if len(edges) == 3:
                    continue

                nodeID1, nodeID2 = int(edges[0]), int(edges[1])

                if nodeID1 != nodeID2:  # this condition enables us to avoid self-loops
                    count += 1

                    if nodeID1 not in self.edges:
                        self.edges[nodeID1] = {}

                    if nodeID2 not in self.edges:
                        self.edges[nodeID2] = {}

                    self.edges[nodeID1][nodeID2] = True
                    self.edges[nodeID2][nodeID1] = True

            self.num_edges = count


    def copy(self):
        g = Graph()

        g.num_nodes = self.num_nodes
        g.num_edges = self.num_edges

        g.edges = {}
        g.nodes = {}

        for k in self.edges.keys():
            g.edges[k] = {x : True for x in self.edges[k].keys()}

        for n in self.nodes.values():
            g.nodes[n.number] = n.copy()

        return g


    def degree(self, number):
        if self.nodes[number].degree != -1:
            return self.nodes[number].degree

        self.nodes[number].degree = len(self.edges[number].keys())
        return len(self.edges[number].keys())

    def node_neighbor_order(self, number):
        """
        Returns a list of the neighbors in a cyclic ordering.

        Parameters
        ----------
        number : int
            the label of the node we want the cyclic ordering for

        Returns
        -------
        List
            one of the d cyclic orderings of the nodes around the input node, where d is the degree of the input node

        """
        node = self.nodes[number]

        if node.nbd_order is not None:
            return node.nbd_order

        nodes = []
        for n in self.edges[number].keys():
            nodes.append([self.nodes[n].longitude, self.nodes[n].latitude, self.nodes[n].number])

        if len(nodes) == 0:
            return []
        elif len(nodes) == 1 or len(nodes) == 2:
            return list(self.edges[number].keys())

        center = [node.longitude, node.latitude, node.number]

        node.nbd_order = Graph.angle_order(center, nodes)
        return node.nbd_order

    @staticmethod
    def angle_order(center, points):
        x, y = center[0], center[1]
        arr = []

        for p in points:
            if p[0] == x:
                if p[1] > y:
                    v = math.pi / 2
                elif p[1] < y:
                    v = (3 * math.pi) / 2
                else:
                    raise ValueError('the center of the point and a neighbor have same coordinates')
            elif p[0] > x and p[1] > y:  # then we are in the first quadrant
                v = math.atan((p[1] - y) / (p[0] - x))
            elif (p[0] < x and p[1] >= y) or (p[0] < x and p[1] <= y):  # then we are in the second or third quadrant
                v = math.atan((p[1] - y) / (p[0] - x))
                v += math.pi
            else:  # then we are in the fourth quadrant
                v = math.atan((p[1] - y) / (p[0] - x))
                v += 2 * math.pi

            arr.append([p[2], v])

        arr = sorted(arr, key=lambda x: x[1])
        return [x[0] for x in arr]

    def set_origin(self, window_width, window_height):
        """
        This function is used to help position the drawing with the proper coordinates.

        Parameters
        ----------
        window_width : int
            should be the width of the window used to create drawing of the road network
        window_height : int
            should be the height of the window used to create drawing of the road network

        Returns
        -------
        """
        largest_lat = float("-infinity")
        largest_long = float("-infinity")
        smallest_lat = float("infinity")
        smallest_long = float("infinity")

        for x in self.nodes.values():
            if x.latitude > largest_lat:
                largest_lat = x.latitude

            if x.latitude < smallest_lat:
                smallest_lat = x.latitude

            if x.longitude > largest_long:
                largest_long = x.longitude

            if x.longitude < smallest_long:
                smallest_long = x.longitude

        self.origin = (smallest_long, largest_lat)
        self.longitude_diff = largest_long - smallest_long
        self.latitude_diff = largest_lat - smallest_lat

        self._rescale(window_width, window_height)

    def _rescale(self, window_width, window_height):
        """
        This function is used to spread the nodes out.

        Parameters
        ----------
        window_width : int
            should be the width of the window used to create drawing of the road network
        window_height : int
            should be the height of the window used to create drawing of the road network

        Returns
        -------
        """
        adjustment = min(window_width / self.longitude_diff, window_height / self.latitude_diff)

        for x in self.nodes.values():
            x.longitude = math.fabs(x.longitude - self.origin[0])
            x.latitude = math.fabs(x.latitude - self.origin[1])

            x.longitude *= adjustment
            x.latitude *= adjustment

    def get_connected_subgraph(self, size, start):
        """
        This function uses BFS to obtain a connected subgraph.  It starts from the vertex numbered start and continues
        to add vertices to the subgraph until size nodes have been found.

        Parameters
        ----------
        size : int
            the size of the connected subgraph being returned
        start : int
            the number of the vertex to start the BFS

        Returns
        -------
        Graph
            this is the subgraph of the road network
        """
        try:
            start = self.nodes[start]
        except IndexError:
            raise IndexError('start was not in the range of the number of vertices')

        count = 1
        queue = deque()
        visited = {start : True}

        new_graph = Graph()

        queue.appendleft(start)

        while queue:
            next = queue.pop()
            new_graph.nodes[next.number] = next  # add vertex to the subgraph to be returned
            new_graph.num_nodes += 1

            for neighbor in self.edges[next.number].keys():

                if self.nodes[neighbor] not in visited and count < size:
                    queue.appendleft(self.nodes[neighbor])
                    visited[self.nodes[neighbor]] = True

                    if neighbor not in new_graph.edges:
                        new_graph.edges[neighbor] = {}

                    if next.number not in new_graph.edges:
                        new_graph.edges[next.number] = {}

                    new_graph.edges[neighbor][next.number] = True
                    new_graph.edges[next.number][neighbor] = True
                    new_graph.num_edges += 1

                    count += 1

        return new_graph

    def reduce_region(self, x1, y1, x2, y2, window_width, window_height):
        new_graph = Graph()

        within_range = lambda n: n.longitude > x1 and n.longitude < x2 and n.latitude > y1 and n.latitude < y2
        new_graph.nodes = {n : self.nodes[n] for n in self.nodes.keys() if within_range(self.nodes[n])}

        new_graph.num_nodes = self.num_nodes
        new_graph.num_edges = 0

        for e in self.edges.keys():
            if e in new_graph.nodes:
                new_edges = {k : True for k in self.edges[e].keys() if k in new_graph.nodes}

                if new_edges:
                    new_graph.edges[e] = new_edges
                    new_graph.num_edges += len(new_edges)

        new_graph.num_edges >>= 2

        adjustment = min(window_width / abs(x1 - x2), window_height / abs(y1 - y2))

        for x in self.nodes.values():
            x.longitude -= x1
            x.latitude -= y1

            x.longitude *= adjustment
            x.latitude *= adjustment

        return new_graph
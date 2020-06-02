from tkinter import *
from roadnetowrkmatching.graph import *
import math


# If you want to make the drawing bigger, change the window_width and window_height variables below
window_width = 1000
window_height = 750


def add_nodes(canv, g, color, matched_id, translate):
    """
    Draws the nodes for the graph g on a tkinter Canvas object.  If color is set to true, then the nodes that have been
    matched are drawn as blue nodes and the ones that haven't been matched are drawn red.  If matched_id is set to true,
    then the drawing will include the value of matched_id for each of the vertices (helps us see which vertices were
    mapped together).

    Parameters
    ----------
    canv : tkinter.Canvas
        the object in which the drawing of the graph is made with
    g : Graph
        the graph to create the drawing of
    color : boolean
        indicates whether or not to color the matched/unmatched vertices or leave them black
    matched_id : boolean
        indicates whether or not to add the values of matched_id above the vertices

    Returns
    -------
    """
    epsilon = 2.5
    for vertex in g.nodes.values():
        if color:
            fill_color = "blue" if vertex.matched else "red"

            # if vertex.draw_green:
            #     fill_color = "green"
            # else:
            #     fill_color = "black"
        else:
            fill_color = "black"

        canv.create_oval(vertex.longitude - epsilon + translate[0], vertex.latitude - epsilon + translate[1],
                         vertex.longitude + epsilon + translate[0], vertex.latitude + epsilon + translate[1],
                         fill=fill_color)


def add_labels(canv, g, matched_id, translate):
    for vertex in g.nodes.values():
        if matched_id and (vertex.draw_matched_number or vertex.draw_yellow_label):
            col = "white" if vertex.draw_matched_number else "yellow"

            canv.create_rectangle(vertex.longitude - 10, vertex.latitude - 15,
                                  vertex.longitude + 8, vertex.latitude ,
                                  fill=col)
            canv.create_text(vertex.longitude + translate[0],
                             vertex.latitude - 8 + translate[1],
                             text=str(vertex.matched_id))


def add_edges(canv, g, translation):
    for i, vertex in enumerate(g.edges.keys()):
        for neighbor in g.edges[vertex].keys():
            canv.create_line(g.nodes[vertex].longitude + translation[0], g.nodes[vertex].latitude + translation[1],
                             g.nodes[neighbor].longitude + translation[0], g.nodes[neighbor].latitude + translation[1],
                             width=1)


def add_seed_names(canv, g):
    for vertex in g.nodes.values():
        if vertex.special_name is not None:
            canv.create_rectangle(vertex.longitude - 10, vertex.latitude - 15,
                                  vertex.longitude + 10, vertex.latitude,
                                  fill="white")
            canv.create_text(vertex.longitude, vertex.latitude - 8, text=vertex.special_name)


def draw_network(file_name, draw_subgraph):
    """
    The main method used to create the drawing of the road network.  In order to change the size of the network,
    the first argument to get_connected_subgraph indicates the number of nodes of the network being drawn and the
    second argument acts like a random seed (if changed, it will select a different subgraph to be drawn).  See the
    function get_connected_subgraph for more details.

    Parameters
    ----------
    file_name : str
        name of the file to be read; must be from http://www.dis.uniroma1.it/challenge9/data/tiger/

    Returns
    -------
    """
    g = Graph(file_name)

    if draw_subgraph:
        g = g.get_connected_subgraph(250, 1000)

    draw_graph(g, False, False, False)


def connect_graphs(g1, g2, first_rect, second_rect):
    master = Tk()
    master.wm_title("road network")

    canv = Canvas(master, width=window_width, height=window_height)
    canv.pack()
    add_grid(canv)

    g1.set_origin(window_width, window_height)
    g2.set_origin(window_width, window_height)

    print("old g1: ", len(g1.nodes))
    print("old g2: ", len(g2.nodes))

    g1 = g1.reduce_region(first_rect[0], first_rect[1], first_rect[2], first_rect[3], window_width/2, window_height)
    g2 = g2.reduce_region(second_rect[0], second_rect[1], second_rect[2], second_rect[3], window_width/2, window_height)

    print("new g1: ", len(g1.nodes))
    print("new g2: ", len(g2.nodes))

    matched = {}

    for n in g1.nodes.values():
        if n.matched_id != -1:
            matched[n.matched_id] = n

    for n in g2.nodes.values():
        if n.matched_id != -1:
            if n.matched_id in matched:
                n.draw_green = True
                matched[n.matched_id].draw_green = True

    add_edges(canv, g1, [0,0])
    add_edges(canv, g2, [window_width/2, 0])

    add_nodes(canv, g1, True, False, [0,0])
    add_nodes(canv, g2, True, False, [window_width/2, 0])

    add_labels(canv, g1, True, [0,0])
    add_labels(canv, g2, True, [window_width/2, 0])

    canv.create_line(window_width/2, 0, window_width/2, window_height)

    mainloop()


def draw_graph(g, reduce, color, matched_id, x1=-1, y1=-1, x2=-1, y2=-1):
    """
    This function takes in an instance of the Graph class and creates the drawing of it using tkinter.  If reduce is
    True, then the drawing uses x1, y1, x2, y2 to reduce the drawing to the rectangle created by (x1, y1) and (x2, y2),
    where (x1, y1) determines the upper left-hand corner and (x2, y2) determines the lower right-hand corner of the
    rectangle desired to draw.  For the definition of color and matched_id, read the description for function add_nodes.

    Parameters
    ----------
    g : Graph
        road network to be drawn
    reduce : boolean
        description of this argument is given above
    color : boolean
        description of this argument is given in the description of the function add_nodes
    matched_id : boolean
        description of this argument is given in the description of the function add_nodes
    x1 : int
        x-coordinate for the upper left-hand corner of the rectangle to reduce the drawing to
    y1 : int
        y-coordinate for the upper left-hand corner of the rectangle to reduce the drawing to
    x2 : int
        x-coordinate for the lower right-hand corner of the rectangle to reduce the drawing to
    y2 : int
        y-coordinate for the lower right-hand corner of the rectangle to reduce the drawing to

    Returns
    -------
    """
    master = Tk()
    master.wm_title("road network" + str(x1) + " " + str(x2))

    canv = Canvas(master, width=window_width, height=window_height)
    canv.pack()
    add_grid(canv)

    g.set_origin(window_width, window_height)

    if reduce:
        if x1 == -1 or y1 == -1 or x2 == -1 or y2 == -1:
            raise ValueError("New region not set.")

        graph_to_draw = g.reduce_region(x1, y1, x2, y2, window_width, window_height)
    else:
        graph_to_draw = g

    add_nodes(canv, graph_to_draw, color, matched_id, [0,0])
    add_edges(canv, graph_to_draw, [0, 0])
    add_seed_names(canv, graph_to_draw)
    add_labels(canv, g, matched_id, [0, 0])

    mainloop()


def add_grid(canv):
    """
    Adds dashed blue lines to allow the user to see the coordinates of the drawings.

    Parameters
    ----------
    canv : tkinter.Canvas
        the object to create the drawing on

    Returns
    -------
    """
    unit_len = 100
    for i in range(0, math.floor(window_width), unit_len):
        canv.create_line(i, 0, i, window_height, fill="blue", dash=(2, 2))

    for i in range(0, math.floor(window_height), unit_len):
        canv.create_line(0, i, window_width, i, fill="blue", dash=(2, 2))


if __name__ == "__main__":
    # draw_network("/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/oregon/untouched/2006/TGR41015-graph.txt", False)
    # draw_network("/Users/manueltorres/PycharmProjects/approximate_road_matching/2006-TGR06011-graph.txt", False)

    draw_network("/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/graphs_to_test_tiger_vs_shape/tl_2010_06005_roads/tl_2010_06005_roads-graph.txt", False)
    # draw_network("/Users/manueltorres/Desktop/tl_2010_06005_roads-graph.txt", False)
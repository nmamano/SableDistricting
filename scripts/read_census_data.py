import shapefile
import os


class Point:
    def __init__(self, nx, ny):
        self.x = nx
        self.y = ny

    def __hash__(self):
        return hash((self.x, self.y))  # relies on the quality of the native hashing function for tuples

    def __eq__(self, other):
        return other.x == self.x and other.y == self.y


def create_dimacs_file(file_name, is_shapefile):
    """
    Main function used to convert either a TIGER/Line file or a shapefile into the same format as the graphs from the
    DIMACS competition.

    Parameters
    ----------
    file_name : str
        path to the file to convert
    is_shapefile : bool
        if true, then the type of file at the path file_name is a shapefile; otherwise, it is a TIGER/Line file

    Returns
    -------
    """
    if is_shapefile:
        l, edge_count = get_coords_from_shapefile(file_name)
    else:
        l, edge_count = get_coords_from_tiger(file_name)

    create(file_name, l, edge_count)


def split_long_lat(long_lat):
    """
    Takes in a string that looks like "-02398470+09384779" in order to obtain the first value and the second value
    of the string.  In this case, the longitude is -02398470 and the latitude is +09384779.  This function is needed
    because the TIGER/Line files have the longitudes and latitudes in this format.

    Parameters
    ----------
    long_lat : str
        has the format listed in the description of the function

    Returns
    -------
    int, int
        the first int is the longitude of the point and the second is the latitude of the point

    """
    long_x = ""
    lat_y = ""

    reached_center = False

    for i, c in enumerate(long_lat):
        if c.isdigit() == False and i != 0:
            reached_center = True

        if reached_center:
            lat_y += c
        else:
            long_x += c

    if reached_center:
        return int(long_x), int(lat_y)
    else:
        return None, None


def split_long_str(to_split):
    start = ""
    end = ""

    reading_first = False
    reading_second = False

    for c in to_split:
        if c == "-":
            if reading_first == False:
                reading_first = True
                start += c
            else:
                reading_first = False
                reading_second = True
                end += c

        elif reading_first:
            start += c
        elif reading_second:
            end += c

    return start, end


def get_coords_from_shapefile(file_name):
    """
    Has the exact same function as get_coords_from_tiger (more detailed description in get_coords_from_tiger comments).

    Parameters
    ----------
    file_name : str
        the path to the TIGER/Line file

    Returns
    -------
    Dict
        key/value pairs are described in the comments for get_coords_from_tiger
    """
    sf = shapefile.Reader(file_name)

    all_nodes = {}
    edge_count = 0

    for s in sf.shapes():
        first_point = Point(s.points[0][0], s.points[0][1])
        last_point = Point(s.points[-1][0], s.points[-1][1])

        if first_point not in all_nodes:
            all_nodes[first_point] = []
        if last_point not in all_nodes:
            all_nodes[last_point] = []

        if first_point not in all_nodes[last_point]:
            edge_count += 1
            all_nodes[last_point].append(first_point)

        if last_point not in all_nodes[first_point]:
            edge_count += 1
            all_nodes[first_point].append(last_point)

        # Shapefiles represent road segments as sequences of points in the plane. This is useful because a curved road
        # can be approximated by a sequence of short, straight lines. However, when only needing the intersections, the
        # following lines of code are not needed.

        # prev_point = None
        # for i, p in enumerate(s.points):
            # curr_point = Point(p[0], p[1])
            #
            # if i != 0:
            #     # should eventually change the lists to be dictionaries
            #     if prev_point not in all_nodes:
            #         all_nodes[prev_point] = []
            #     if curr_point not in all_nodes:
            #         all_nodes[curr_point] = []
            #
            #     if curr_point not in all_nodes[prev_point]:
            #         all_nodes[prev_point].append(curr_point)
            #
            #     if prev_point not in all_nodes[curr_point]:
            #         all_nodes[curr_point].append(prev_point)
            #
            # prev_point = Point(p[0], p[1])

    return all_nodes, edge_count


def get_coords_from_tiger(file_name):
    """
    Takes in a path to a TIGER/Line file and outputs a dictionary.  The keys are a Point (a class defined above) where
    each Point corresponds to either a starting point or an ending point of a line segment in
    the TIGER/Line file.  The values of the dictionary are the interesting part.  The values are lists of Points.  A
    Point b is in the list of the key for Point c if the opposite endpoint of the line segment that b belongs to has the
    same coordinates as c.

    Parameters
    ----------
    file_name : str
        the path to the TIGER/Line file

    Returns
    -------
    Dict
        key/value pairs are described above

    """
    f = open(file_name, 'r')  # f is the file we are reading from

    all_nodes = {}
    edge_count = 0

    for line in f:
        split_line = line.split()

        is_road = False
        s = None  # s corresponds to the starting node
        e = None  # e corresponds to the ending node

        for col in split_line:
            column_length = len(col)

            if column_length == 3:
                if col[0] == 'A' and col[0].isalpha() and col[1].isdigit() and col[2].isdigit():
                    is_road = True

            if column_length > 30 and is_road:
                start, end = split_long_str(col)

                x, y = split_long_lat(start)
                s = Point(x, y)

                x, y = split_long_lat(end)
                e = Point(x, y)
            elif column_length > 14 and col[0] == "-" and is_road:
                if s is None:
                    x, y = split_long_lat(col)

                    if x is None:
                        continue

                    s = Point(x, y)
                else:
                    x, y = split_long_lat(col)
                    e = Point(x, y)

        if is_road:
            edge_count += 1
            if s not in all_nodes:
                all_nodes[s] = []
            if e not in all_nodes:
                all_nodes[e] = []

            if e not in all_nodes[s]:
                all_nodes[s].append(e)

            if s not in all_nodes[e]:
                all_nodes[e].append(s)

    return all_nodes, edge_count / 2


def create(file_name, all_nodes, edge_count):
    name = ""
    for c in file_name:
        if c == ".":
            break
        else:
            name += c

    output = open(name + "-graph.txt", 'w')
    output.write(str(len(all_nodes)) + "\n")

    labels = {}  # dictionary that will map the vertices to their label
    for i, v in enumerate(all_nodes):
        output.write(str(i) + " " + str(v.x) + " " + str(v.y) + "\n")
        labels[v] = i

    output.write(str(edge_count) + "\n")
    for node in all_nodes:
        for adjacent_node in all_nodes[node]:
            output.write(str(labels[node]) + " " + str(labels[adjacent_node]) + " " + "\n")

    output.close()


def merge(files, file_name):
    """
    Takes a list, files, as input and merges them into one text file.  The files are expected to already be in the
    form of the files from the DIMACS competition.  If they are not in the DIMACS competition format, that is what the
    function create_dimacs_file is for.

    Usually, this is useful for the case where you want a graph for California, but you only have the DIMACS files for
    all of the counties in California, so this function can be used to combine the counties into one state.

    Parameters
    ----------
    files : List[str]
        the strings are the paths to the files to be merged
    file_name : str
        the name of the file that will contain all of the merged files

    Returns
    -------
    """
    open_files = [open(f, 'r') for f in files]
    all_nodes = {}
    num_edges = 0

    for f in open_files:
        coords = {}

        for i, line in enumerate(f):
            if i == 0:
                continue

            split_line = line.split()

            if len(split_line) == 1:
                num_edges += int(split_line[0])

            split_line = [int(l) for l in split_line]
            coords[split_line[0]] = (split_line[1], split_line[2])

        for line in f:
            split_line = line.split()

            first_coords = coords[int(split_line[0])]
            second_coords = coords[int(split_line[1])]

            first_p = Point(first_coords[0], first_coords[1])
            second_p = Point(second_coords[0], second_coords[1])

            if first_p not in all_nodes:
                all_nodes[first_p] = []

            if second_p not in all_nodes:
                all_nodes[second_p] = []

            all_nodes[first_p].append(second_p)
            all_nodes[second_p].append(first_p)

    create(file_name, all_nodes, num_edges)

    for f in open_files:
        f.close()


def convert_and_remove_for_tiger(path):
    """
    The downloaded folders from the US Census Bureau(?) [not sure about where it came from] include a bunch of
    extra files as well as the TIGER Line files. This function takes in a system path to a directory of one of these
    folders, it then finds the TIGER Line file, converts it to the DIMACS format, and then deletes the old files.

    This function was used as a convenience for the specific workflow of the first use of this code and might not find
    much use from others.

    Parameters
    ----------
    path : str
        the path of where the TIGER line folder is located

    Returns
    -------
    None

    """
    for file in os.listdir(path):
        if file == ".DS_Store":
            continue

        reading = False
        ext = ""
        for c in file:
            if reading:
                ext += c

            if c == ".":
                reading = True

        if ext == "RT1":
            tiger_name = path + file

            print(tiger_name)

            create_dimacs_file(tiger_name, False)

    for file in os.listdir(path):
        if file == ".DS_Store":
            continue

        reading = False
        ext = ""
        for c in file:
            if reading:
                ext += c

            if c == ".":
                reading = True

        if ext != "txt":
            os.remove(path + file)


if __name__ == "__main__":
    # The following lines of code are just example of how to use the above functions

    # convert_and_remove_for_tiger("/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/california/untouched/2006/")

    # convert_and_remove_for_tiger("/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/california/2006/")

    # tiger_name = "/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/california/tgr06107/TGR06107.RT1"
    # create_dimacs_file(tiger_name, False)
    #
    # tiger_name = "/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/california/TGR06107 (1)/TGR06107.RT1"
    # create_dimacs_file(tiger_name, False)

    # tiger_name = "test_files/oregon/TGR41051.RT1"
    # create_dimacs_file(tiger_name, False)

    # files_to_merge = []
    #
    # for file in os.listdir("/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/oregon"):
    #     if file == ".DS_Store":
    #         continue
    #
    #     files_to_merge.append("test_files/oregon/" + file)

    # tiger_name = "/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/tgr06003-2000/TGR06003.RT1"
    # create_dimacs_file(tiger_name, False)

    shape_name = "/Users/manueltorres/PycharmProjects/approximate_road_matching/roadnetowrkmatching/test_files/graphs_to_test_tiger_vs_shape/tl_2010_06005_roads/tl_2010_06005_roads"
    create_dimacs_file(shape_name, True)

    # tiger_names = ["TGR44001/TGR44001.RT1", "TGR44003/TGR44003.RT1", "TGR44005/TGR44005.RT1", "TGR44007/TGR44007.RT1", "TGR44009/TGR44009.RT1"]
    #
    # for tiger_name in tiger_names:
    #     create_dimacs_file(tiger_name, False)
    #
    # new_files = [f + "-graph.txt" for f in tiger_names]
    # merge(files_to_merge, "test.txt")
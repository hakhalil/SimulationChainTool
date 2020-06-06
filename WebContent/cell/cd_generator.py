#!/usr/bin/env python
# coding: utf-8

import argparse
import csv
import os
from PIL import Image, ImageDraw

COLOR_BACKGROUND = (0, 0, 0, 255)
COLOR_OBSTACLE = (255, 255, 255, 255)

BACKGROUND_ID = 0
OBSTACLE_ID = 1


def parse_args():
    parser = argparse.ArgumentParser(description='Auxiliar script to generate Cell-DEVS environments')

    parser.add_argument('-i', '--in_file', type=str, required=True, help='Input image')
    parser.add_argument('-c', '--crop', action="store_true", help='Crop image borders if no obstacle detected')
    parser.add_argument('-b', '--back_color', type=str, default="255,255,255",
                        help='Background color to separate obstacles')
    parser.add_argument('-d', '--delay', type=str, default=1000, help='Default delay')
    parser.add_argument('-n', '--neighborhood', type=str, default="vn",
                        help='Neighborhood type (moore, vn, emoore, evn)')
    parser.add_argument('-m', '--top_name', type=str, help='Name of the resulting files')
    parser.add_argument('-o', '--out_path', type=str, default="out/", help='Output path')
    parser.add_argument('-r', '--rules_file', type=str, default="templates/default_rules.inc",
                        help='File with the rules to include in the main file')
    parser.add_argument('-t', '--tolerance', type=int, default=10, help='Tolerance when separating background')
    parser.add_argument('-w', '--width', type=int, help='Width of the resulting model')
    parser.add_argument('-p', '--padding', type=int,
                        help='Add a padding to the image before generate the Cell-DEVS environment')
    parser.add_argument('-rw', '--revit_width', type=int,
                        help='Width of the intermediate image generated with the Revit walls information')
    parser.add_argument('-rl', '--revit_line_width', type=int,
                        help='Width of the lines in the image generated with the Revit walls information')
    parser.add_argument('-bv', '--back_value', type=int, default=0, help='Value for background cells in .val output file')
    parser.add_argument('-ov', '--obst_value', type=int, default=1, help='Value for obstacle cells in .val output file')

    return parser.parse_args()


def almost_equal(v1, v2, ediff=20):
    for i in range(len(v1)):
        if abs(v1[i] - v2[i]) > ediff:
            return False
    return True


def get_coord_bounds(revit_reader):
    header = next(revit_reader)
    min_x, max_x, min_y, max_y = float("inf"), float("-inf"), float("inf"), float("-inf")

    for row in revit_reader:
        row = dict(zip(header, row))

        if float(row["src_z"]) != 0 or float(row["dst_z"]) != 0:
            continue

        src_x, dst_x = float(row["src_x"]), float(row["dst_x"])
        src_y, dst_y = float(row["src_y"]), float(row["dst_y"])
        min_x = min(min_x, src_x, dst_x)
        max_x = max(max_x, src_x, dst_x)
        min_y = min(min_y, src_y, dst_y)
        max_y = max(max_y, src_y, dst_y)

    return min_x, max_x, min_y, max_y


def revit_csv_to_img(revit_csv, img_width, img_line_width):
    csv_file = open(revit_csv, "r")
    csv_reader = csv.reader(csv_file, delimiter=",")

    min_x, max_x, min_y, max_y = get_coord_bounds(csv_reader)

    if img_width is None:
        img_width = int(20 * (max_x - min_x))

    if img_line_width is None:
        img_line_width = 10

    img_height = int(img_width * ((max_y - min_y) / (max_x - min_x)))
    im = Image.new(mode="RGB", size=(img_width, img_height))

    csv_file.seek(0)
    header = next(csv_reader)
    imd = ImageDraw.Draw(im)

    get_im_x = lambda x: int(((x - min_x) / (max_x - min_x)) * img_width)
    get_im_y = lambda y: int(((y - min_y) / (max_y - min_y)) * img_height)

    for row in csv_reader:
        row = dict(zip(header, row))

        if float(row["src_z"]) != 0 or float(row["dst_z"]) != 0:
            continue

        src_x, dst_x = get_im_x(float(row["src_x"])), get_im_x(float(row["dst_x"]))
        src_y, dst_y = get_im_y(float(row["src_y"])), get_im_y(float(row["dst_y"]))
        shape = [(src_x, src_y), (dst_x, dst_y)]
        print(shape)
        imd.line(shape, fill="white", width=img_line_width)

    return im


def empty_row(img, row_idx, back_color, tolerance):
    width = img.width
    pixels = img.load()

    for col_idx in range(width):
        if not almost_equal(pixels[col_idx, row_idx][:3], back_color[:3], tolerance):
            return False
    return True


def empty_col(img, col_idx, back_color, tolerance):
    height = im.height
    pixels = img.load()

    for row_idx in range(height):
        if not almost_equal(pixels[col_idx, row_idx][:3], back_color[:3], tolerance):
            return False
    return True


if __name__ == '__main__':
    args = parse_args()

    back_color = list(map(int, args.back_color.split(",")))
    if not args.top_name:
        args.top_name = os.path.splitext(os.path.basename(args.in_file))[0]

    os.makedirs(os.path.join(args.out_path, args.top_name), exist_ok=True)

    if args.in_file.endswith(".csv"):
        im = revit_csv_to_img(args.in_file, args.revit_width, args.revit_line_width)
    else:
        im = Image.open(args.in_file)

    if args.crop:
        width, height = im.size

        left = 0
        right = width - 1
        top = 0
        bottom = height - 1

        while left < width and empty_col(im, left, back_color, args.tolerance): left += 1
        while right > 0 and empty_col(im, right, back_color, args.tolerance): right -= 1
        while top < height and empty_row(im, top, back_color, args.tolerance): top += 1
        while bottom > 0 and empty_row(im, bottom, back_color, args.tolerance): bottom -= 1

        im = im.crop((left, top, right, bottom))

    if args.padding:
        new_im = Image.new("RGB", (im.width + args.padding*2, im.height + args.padding*2), tuple(back_color[:3]))
        new_im.paste(im, (args.padding, args.padding))
        im = new_im

    im.save("tmp.png")
    width, height = im.size

    cd_width = args.width
    cd_height = int(cd_width * (height / width))
    im_res = im.resize((cd_width, cd_height))
    print("Cell-DEVS model size: (%d, %d)" % (cd_height, cd_width))

    # Generation of the identifiers matrix
    pixels = im_res.convert('RGB').load()
    mat_id = []

    back_val = args.back_value
    obst_val = args.obst_value

    for i in range(im_res.size[0]):
        mat_id.append([])
        for j in range(im_res.size[1]):
            if almost_equal(pixels[i, j][:3], back_color[:3], args.tolerance):
                pixels[i, j] = COLOR_BACKGROUND
                mat_id[-1].append(back_val)
            else:
                pixels[i, j] = COLOR_OBSTACLE
                mat_id[-1].append(obst_val)

    im_res.save(os.path.join(args.out_path, args.top_name, args.top_name + ".png"))

    # Generation of initial values file (.val)
    with open(os.path.join(args.out_path, args.top_name, args.top_name + ".val"), "w") as out:
        for i in range(len(mat_id)):
            for j in range(len(mat_id[i])):
                if mat_id[i][j] == obst_val:
                    line = "(%d, %d) = %d\n" % (j, i, obst_val)
                    out.write(line)

    # Generation of main file from template (.ma)
    with open("templates/template.ma", "r") as template:
        ma = template.read()

    with open("templates/neighbors_%s.inc" % args.neighborhood, "r") as nei_template:
        neighborhood = nei_template.read()

    with open(args.rules_file, "r") as rules_template:
        rules = rules_template.read()

    neighborhood = neighborhood.replace("id", args.top_name)

    ma = ma.format(name=args.top_name,
                   width=cd_width,
                   height=cd_height,
                   delay=args.delay,
                   initial_value=back_val,
                   val_file=args.top_name + ".val",
                   neighbors=neighborhood)

    with open(os.path.join(args.out_path, args.top_name, args.top_name + ".ma"), "w") as out:
        out.write(ma)
        out.write(rules)

    # Generation of palette file (.pal)
    pal_content = ""
    pal_line = "[%d;%d] %d %d %d\n"

    pal_content += pal_line % ((back_val, back_val + 1) + COLOR_BACKGROUND[:3])
    pal_content += pal_line % ((obst_val, obst_val + 1) + COLOR_OBSTACLE[:3])

    with open(os.path.join(args.out_path, args.top_name, args.top_name + ".pal"), "w") as out:
        out.write(pal_content)

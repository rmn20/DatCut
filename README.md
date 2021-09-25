# DatCut
Broken indev version of [PlaneCut](https://github.com/rmn20/PlaneCut) color quantization/palette generation program, that appeared in [this](https://vk.com/h_o_m_e_m_e_d_i_a?w=wall-201183049_89) post.

# How to use
````
Enter the paths to the images as arguments.
Use -p to change palette size. (from 1 to 256 colors) (default is 256)
Use -fastlsr 0/1 to use fast least square regression method
Use -dither 0/1 to enable dithering. (default is 0)

DatCut test.png -p 8 -dither 1 -fastlsr 1
````

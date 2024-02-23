[![INFORMS Journal on Computing Logo](https://INFORMSJoC.github.io/logos/INFORMS_Journal_on_Computing_Header.jpg)](https://pubsonline.informs.org/journal/ijoc)

# Efficient and Flexible Long-Tail Recommendation Using Cosine Patterns

This archive is distributed in association with the [INFORMS Journal on
Computing](https://pubsonline.informs.org/journal/ijoc)

The purpose of this repository is to share the data and scripts reported in the paper 
[Efficient and Flexible Long-Tail Recommendation Using Cosing Patterns](https://doi.org/??/ijoc.2021.0194) by Y. Wang, J. Wu, Z. Wu, and G. Adomavicius. 

## Cite

To cite the contents of this repository, please cite both the paper and this repo, using their respective DOIs.

https://doi.org/??/ijoc.2022.0194

https://doi.org/??/ijoc.2022.0194.cd

Below is the BibTex for citing this snapshot of the respoitory.

```
@article{??,
  author =        {Wang, Yaqiong and Wu, Junjie and Wu, Zhiang and Adomavicius, Gediminas},
  publisher =     {INFORMS Journal on Computing},
  title =         {{Efficient and Flexible Long-Tail Recommendation Using Cosing Patterns}},
  year =          {2024},
  doi =           {??/ijoc.2022.0194.cd},
  note =          {Available for download at {https://github.com/INFORMSJoC/2022.0194}},
}  
```

## Data
Data sets used for experiments in this paper is available in the data directory.

## Replication

You will need java compiler to run the code.

To execute CP-tree based recommendation, run the following:
```
java GOPC_Tree patternFile trainingFile resultFile topk
```
Sample input file format:
 - patternFile.txt: pattern_items cosine_value
   ```
   A B 0.5
   A B C 0.3
   A E G 0.4
   ```
 - trainingFile.txt: uid itemids
   ```
   u1 A B E G
   u2 B C F
   U3 A D H J
   ```

## Support
Please contact [Yaqiong Wang](ywang31@scu.edu) if you have any questions.

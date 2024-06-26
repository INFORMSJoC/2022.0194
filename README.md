[![INFORMS Journal on Computing Logo](https://INFORMSJoC.github.io/logos/INFORMS_Journal_on_Computing_Header.jpg)](https://pubsonline.informs.org/journal/ijoc)

# Efficient and Flexible Long-Tail Recommendation Using Cosine Patterns

This archive is distributed in association with the [INFORMS Journal on
Computing](https://pubsonline.informs.org/journal/ijoc) under the [MIT License](LICENSE).

The purpose of this repository is to share the data and scripts reported in the paper 
[Efficient and Flexible Long-Tail Recommendation Using Cosing Patterns](https://doi.org/10.1287/ijoc.2022.0194) by Y. Wang, J. Wu, Z. Wu, and G. Adomavicius. 

## Cite

To cite the contents of this repository, please cite both the paper and this repo, using their respective DOIs.

https://doi.org/10.1287/ijoc.2022.0194

https://doi.org/10.1287/ijoc.2022.0194.cd

Below is the BibTex for citing this snapshot of the respoitory.

```
@article{ijoc.2022.0194,
  author =        {Wang, Yaqiong and Wu, Junjie and Wu, Zhiang and Adomavicius, Gediminas},
  publisher =     {INFORMS Journal on Computing},
  title =         {Efficient and Flexible Long-Tail Recommendation Using Cosing Patterns},
  year =          {2024},
  doi =           {10.1287/ijoc.2022.0194.cd},
  note =          {Available for download at {https://github.com/INFORMSJoC/2022.0194}},
}  
```

## Data
Data sets used for experiments in this paper are described in the [data](https://github.com/Yaqiong-Wang/2022.0194/tree/main/data) directory.

## Replication

The [scripts](https://github.com/Yaqiong-Wang/2022.0194/tree/main/scripts) directory contains the codes that generate cosine pattern based recommendations using either single node or spark-based distributed computing.

You will need java compiler to run the code.

- [Mptree.java](https://github.com/Yaqiong-Wang/2022.0194/tree/main/scripts/Singlenode_MPTrees%20(originalversion)/src/RefindCore) corresponds to the Basic Recommendation Scheme described in Section 3.3 in the paper.
- [GOPC_Tree.java](https://github.com/Yaqiong-Wang/2022.0194/tree/main/scripts/Singlenode_GOPC/src/stand_alone_with_tree) corresponds to the Cosine-Pattern Tree Traversal Approach described in Section 3.4 in the paper.
- [GOPC_Tree_Spark.java](https://github.com/Yaqiong-Wang/2022.0194/tree/main/scripts/Distributed_spark_GOPC(including%20cp%20mining%20and%20tree%20matching)/src/main/java/test) corresponds to the Parallelization of the Proposed Approach described in Section 3.5 in the paper.

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
   u3 A D H J
   ```
## Baselines

We used the following publicly available libraries for baseline algorithms:
- [MyMediaLite](http://www.mymedialite.net/): UserKNN (called UCF in our paper), ItemKNN (ICF), BPRMF (BPR), WRMF (WRMF)
- [Surprise](https://surprise.readthedocs.io/en/stable/matrix_factorization.html#surprise.prediction_algorithms.matrix_factorization.SVD): SVD
- [Apriori](https://borgelt.net/apriori.html): FP, AR, PAR
- [VAE_CF](https://github.com/dawenl/vae_cf/tree/master): Deep Learning (called DL in our paper)
- [Long-Tail-GAN](https://github.com/CrowdDynamicsLab/NCF-GAN): Long-tail recommendation (called LNCF in our paper)

## Support
Please contact [Yaqiong Wang](ywang31@scu.edu) if you have any questions.

# LiSSA: A Framework for Generic Traceability Link Recovery

<img src=".github/images/approach.svg" alt="Approach Overview" style="width: 100%; background-color: white; border-radius: 8px; padding: 10px; display: block; margin: 0 auto;" /><br/>


Welcome to the LiSSA project!
This framework leverages Large Language Models (LLMs) enhanced through Retrieval-Augmented Generation (RAG) to establish traceability links across various software artifacts.

## Overview

In software development and maintenance, numerous artifacts such as requirements, code, and architecture documentation are produced.
Understanding the relationships between these artifacts is crucial for tasks like impact analysis, consistency checking, and maintenance.
LiSSA aims to provide a generic solution for Traceability Link Recovery (TLR) by utilizing LLMs in combination with RAG techniques.

The concept and evaluation of LiSSA are detailed in our paper:

> Fuchß, D., Hey, T., Keim, J., Liu, H., Ewald, N., Thirolf, T., & Koziolek, A. (2025). LiSSA: Toward Generic Traceability Link Recovery through Retrieval-Augmented Generation. In Proceedings of the IEEE/ACM 47th International Conference on Software Engineering, Ottawa, Canada.

You can access the paper [here](https://ardoco.de/c/icse25).

## Features

- **Generic Applicability**: LiSSA is designed to recover traceability links across various types of software artifacts, including:
  - Requirements to code
  - Documentation to code
  - Architecture documentation to architecture models

- **Retrieval-Augmented Generation**: By combining LLMs with RAG, LiSSA enhances the accuracy and relevance of the recovered traceability links.

## Getting Started

To get started with LiSSA, follow these steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ArDoCo/LiSSA-RATLR
   cd LiSSA-RATLR
   ```

2. **Install Dependencies**:
   Ensure you have Java JDK 21 or later installed. Then, build the project using Maven:
   ```bash
   mvn clean package
   ```

3. **Run LiSSA**:
   Execute the main application:
   ```bash
   java -jar target/ratlr-*-jar-with-dependencies.jar eval -c config.json
   ```

### Configuration

1. Create a configuration you want to use for evaluation / execution. E.g., you can find configurations [here](https://github.com/ArDoCo/ReplicationPackage-ICSE25_LiSSA-Toward-Generic-Traceability-Link-Recovery-through-RAG/tree/main/LiSSA-RATLR-V2/lissa/configs/req2code-significance). You can also provide a directory containing multiple configurations.
2. Configure your OpenAI API key and organization in a `.env` file. You can use the provided template file as a template [env-template](env-template).
3. LiSSA caches requests in order to be reproducible. The cache is located in the cache folder that can be specified in the configuration.
4. Run `java -jar target/ratlr-*-jar-with-dependencies.jar eval -c configs/....` to run the evaluation. You can provide a JSON or a directory containing JSON configurations.
5. The results will be printed to the console and saved to a file in the current directory. The name is also printed to the console.

### Results of Evaluation / Execution
The results will be stored as markdown files.
A result file can look like below.
It contains the configuration and the results of the evaluation.
Additionally, the LiSSA generate CSV files that contain the traceability links as pairs of identifiers.

```json
## Configuration
{
  "cache_dir" : "./cache-r2c/dronology-dd--102959883",
  "gold_standard_configuration" : {
    "hasHeader" : false,
    "path" : "./datasets/req2code/dronology-dd/answer.csv"
  },
  "... other configuration parameters ..."
}

## Stats
* # TraceLinks (GS): 740
* # Source Artifacts: 211
* # Target Artifacts: 423
## Results
* True Positives: 283
* False Positives: 1286
* False Negatives: 457
* Precision: 0.18036966220522627
* Recall: 0.3824324324324324
* F1: 0.24512776093546992
```

<details>

```json
## Configuration
{
  "cache_dir" : "./cache-r2c/dronology-dd--102959883",
  "gold_standard_configuration" : {
    "hasHeader" : false,
    "path" : "./datasets/req2code/dronology-dd/answer.csv"
  },
  "source_artifact_provider" : {
    "name" : "text",
    "args" : {
      "artifact_type" : "requirement",
      "path" : "./datasets/req2code/dronology-dd/UC"
    }
  },
  "target_artifact_provider" : {
    "name" : "recursive_text",
    "args" : {
      "artifact_type" : "source code",
      "path" : "./datasets/req2code/dronology-dd/CC",
      "extensions" : "java"
    }
  },
  "source_preprocessor" : {
    "name" : "artifact",
    "args" : { }
  },
  "target_preprocessor" : {
    "name" : "artifact",
    "args" : { }
  },
  "embedding_creator" : {
    "name" : "openai",
    "args" : {
      "model" : "text-embedding-3-large"
    }
  },
  "source_store" : {
    "name" : "custom",
    "args" : { }
  },
  "target_store" : {
    "name" : "custom",
    "args" : {
      "max_results" : "20"
    }
  },
  "classifier" : {
    "name" : "reasoning_openai",
    "args" : {
      "model" : "gpt-4o-2024-05-13",
      "seed" : "-102959883",
      "prompt" : "Below are two artifacts from the same software system. Is there a traceability link between (1) and (2)? Give your reasoning and then answer with 'yes' or 'no' enclosed in <trace> </trace>.\n (1) {source_type}: '''{source_content}''' \n (2) {target_type}: '''{target_content}''' ",
      "use_original_artifacts" : "false",
      "use_system_message" : "true"
    }
  },
  "result_aggregator" : {
    "name" : "any_connection",
    "args" : {
      "source_granularity" : "0",
      "target_granularity" : "0"
    }
  },
  "tracelinkid_postprocessor" : {
    "name" : "identity",
    "args" : {
      "reverse" : "false"
    }
  }
}

## Stats
* # TraceLinks (GS): 740
* # Source Artifacts: 211
* # Target Artifacts: 423
## Results
* True Positives: 283
* False Positives: 1286
* False Negatives: 457
* Precision: 0.18036966220522627
* Recall: 0.3824324324324324
* F1: 0.24512776093546992
```

</details>


## Evaluation

LiSSA has been empirically evaluated on three different TLR tasks:

- Requirements to code
- Documentation to code
- Architecture documentation to architecture models

The results indicate that the RAG-based approach can significantly outperform state-of-the-art methods in code-related tasks.
However, further research is needed to enhance its performance for broader applicability.

## Contributing

We welcome contributions from the community! If you're interested in contributing to LiSSA, please read our [Contributing Guide](CONTRIBUTING.md) to get started.

## License

This project is licensed under the MIT License. See the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

LiSSA is developed by researchers from the Modelling for Continuous Software Engineering (MCSE) group of KASTEL - Institute of Information Security and Dependability at the Karlsruhe Institute of Technology (KIT).

For more information about the project and related research, visit our [website](https://ardoco.de/).

---

*Note: This README provides a brief overview of the LiSSA project. For comprehensive details, please refer to [Development.md](Development.md).* 

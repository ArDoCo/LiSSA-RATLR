TEMPLATE_BBB = """
{
  "cache_dir": "./cache-d2c/bigbluebutton-<<SEED>>",
  "gold_standard_configuration": {
    "path": "./datasets/doc2code/bigbluebutton/goldstandards/goldstandard-bigbluebutton.csv",
    "hasHeader": "true"
  },

  "source_artifact_provider" : {
    "name" : "text",
    "args" : {
      "artifact_type" : "software architecture documentation",
      "path" : "./datasets/doc2code/bigbluebutton/text_2021/bigbluebutton_1SentPerLine.txt"
    }
  },
  "target_artifact_provider" : {
    "name" : "recursive_text",
    "args" : {
      "artifact_type" : "source code",
      "path" : "./datasets/doc2code/bigbluebutton/model_2023/code",
      "extensions" : ".java,.sh"
    }
  },
  "source_preprocessor" : {
    "name" : "sentence",
    "args" : { }
  },
  "target_preprocessor" : {
    "name" : "artifact",
    "args" : {
    }
  },
  "embedding_creator" : {
    "name" : "openai",
    "args" : {
      "model": "text-embedding-3-large"
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
      "model" : "gpt-4o-mini-2024-07-18",
      "seed": "<<SEED>>"
    }
  },
  "result_aggregator" : {
    "name" : "any_connection",
    "args" : {
      "source_granularity" : "1",
      "target_granularity" : "0"
    }
  },
  "tracelinkid_postprocessor" : {
    "name" : "sad2code",
    "args" : { }
  }
}
"""

TEMPLATE_MS = """
{
  "cache_dir": "./cache-d2c/mediastore-<<SEED>>",
  "gold_standard_configuration": {
    "path": "./datasets/doc2code/mediastore/goldstandards/goldstandard-mediastore.csv",
    "hasHeader": "true"
  },

  "source_artifact_provider" : {
    "name" : "text",
    "args" : {
      "artifact_type" : "software architecture documentation",
      "path" : "./datasets/doc2code/mediastore/text_2016/mediastore.txt"
    }
  },
  "target_artifact_provider" : {
    "name" : "recursive_text",
    "args" : {
      "artifact_type" : "source code",
      "path" : "./datasets/doc2code/mediastore/model_2016/code",
      "extensions" : ".java,.sh"
    }
  },
  "source_preprocessor" : {
    "name" : "sentence",
    "args" : { }
  },
  "target_preprocessor" : {
    "name" : "artifact",
    "args" : {
    }
  },
  "embedding_creator" : {
    "name" : "openai",
    "args" : {
      "model": "text-embedding-3-large"
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
      "model" : "gpt-4o-mini-2024-07-18",
      "seed": "<<SEED>>"
    }
  },
  "result_aggregator" : {
    "name" : "any_connection",
    "args" : {
      "source_granularity" : "1",
      "target_granularity" : "0"
    }
  },
  "tracelinkid_postprocessor" : {
    "name" : "sad2code",
    "args" : { }
  }
}
"""

TEMPLATE_TS = """
{
  "cache_dir": "./cache-d2c/teastore-<<SEED>>",
  "gold_standard_configuration": {
    "path": "./datasets/doc2code/teastore/goldstandards/goldstandard-teastore.csv",
    "hasHeader": "true"
  },
  "source_artifact_provider" : {
    "name" : "text",
    "args" : {
      "artifact_type" : "software architecture documentation",
      "path" : "./datasets/doc2code/teastore/text_2020/teastore.txt"
    }
  },
  "target_artifact_provider" : {
    "name" : "recursive_text",
    "args" : {
      "artifact_type" : "source code",
      "path" : "./datasets/doc2code/teastore/model_2022/code",
      "extensions" : ".java,.sh"
    }
  },
  "source_preprocessor" : {
    "name" : "sentence",
    "args" : { }
  },
  "target_preprocessor" : {
    "name" : "artifact",
    "args" : {
    }
  },
  "embedding_creator" : {
    "name" : "openai",
    "args" : {
      "model": "text-embedding-3-large"
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
      "model" : "gpt-4o-mini-2024-07-18",
      "seed": "<<SEED>>"
    }
  },
  "result_aggregator" : {
    "name" : "any_connection",
    "args" : {
      "source_granularity" : "1",
      "target_granularity" : "0"
    }
  },
  "tracelinkid_postprocessor" : {
    "name" : "sad2code",
    "args" : { }
  }
}
"""


# Configurations
seeds = ["133742243"]

import os

for seed in seeds:
    os.makedirs("./configs/doc2code", exist_ok=True)
    # Generate
    with open(f"./configs/doc2code/bigbluebutton_{seed}.json", "w") as f:
        f.write(TEMPLATE_BBB.replace("<<SEED>>", seed))
    with open(f"./configs/doc2code/mediastore_{seed}.json", "w") as f:
        f.write(TEMPLATE_MS.replace("<<SEED>>", seed))
    with open(f"./configs/doc2code/teastore_{seed}.json", "w") as f:
        f.write(TEMPLATE_TS.replace("<<SEED>>", seed))

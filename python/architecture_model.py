import torch.nn as nn
import torch.nn.functional as F


class ResNet(nn.Module):
    """
    Residual Network (ResNet) class for policy and value prediction in AlphaZero.

    Attributes:
        device (torch.device): The device to run the model on (CPU or GPU).
        startBlock (nn.Sequential): The initial convolutional block.
        backBone (nn.ModuleList): The list of residual blocks.
        policyHead (nn.Sequential): The policy head for action probabilities.
        valueHead (nn.Sequential): The value head for state evaluation.
    """

    def __init__(self, game, num_resBlocks, num_hidden, device):
        """
        Initializes the ResNet object with the given parameters.

        Args:
            game (Game): The game object that provides game-specific logic.
            num_resBlocks (int): The number of residual blocks.
            num_hidden (int): The number of hidden units in each layer.
            device (torch.device): The device to run the model on (CPU or GPU).
        """
        super().__init__()
        self.device = device

        self.startBlock = nn.Sequential(
            nn.Conv2d(3, num_hidden, kernel_size=3, padding=1),
            nn.BatchNorm2d(num_hidden),
            nn.ReLU()
        )
        self.backBone = nn.ModuleList(
            [ResBlock(num_hidden) for i in range(num_resBlocks)]
        )
        self.policyHead = nn.Sequential(
            nn.Conv2d(num_hidden, 32, kernel_size=3, padding=1),
            nn.BatchNorm2d(32),
            nn.ReLU(),
            nn.Flatten(),
            nn.Linear(32 * game.row_count * game.column_count, game.action_size)
        )
        self.valueHead = nn.Sequential(
            nn.Conv2d(num_hidden, 3, kernel_size=3, padding=1),
            nn.BatchNorm2d(3),
            nn.ReLU(),
            nn.Flatten(),
            nn.Linear(3 * game.row_count * game.column_count, 1),
            nn.Tanh()
        )

        self.to(device)

    def forward(self, x):
        """
        Defines the forward pass of the ResNet.

        Args:
            x (torch.Tensor): The input tensor.

        Returns:
            tuple: A tuple containing the policy and value predictions.
        """
        x = self.startBlock(x)
        for resBlock in self.backBone:
            x = resBlock(x)
        policy = self.policyHead(x)
        value = self.valueHead(x)
        return policy, value


class ResBlock(nn.Module):
    """
    Residual Block class for the ResNet.

    Attributes:
        conv1 (nn.Conv2d): The first convolutional layer.
        bn1 (nn.BatchNorm2d): The first batch normalization layer.
        conv2 (nn.Conv2d): The second convolutional layer.
        bn2 (nn.BatchNorm2d): The second batch normalization layer.
    """

    def __init__(self, num_hidden):
        """
        Initializes the ResBlock object with the given number of hidden units.

        Args:
            num_hidden (int): The number of hidden units in each layer.
        """
        super().__init__()
        self.conv1 = nn.Conv2d(num_hidden, num_hidden, kernel_size=3, padding=1)
        self.bn1 = nn.BatchNorm2d(num_hidden)
        self.conv2 = nn.Conv2d(num_hidden, num_hidden, kernel_size=3, padding=1)
        self.bn2 = nn.BatchNorm2d(num_hidden)

    def forward(self, x):
        """
        Defines the forward pass of the ResBlock.

        Args:
            x (torch.Tensor): The input tensor.

        Returns:
            torch.Tensor: The output tensor after applying the residual block.
        """
        residual = x
        x = F.relu(self.bn1(self.conv1(x)))
        x = self.bn2(self.conv2(x))
        x += residual
        x = F.relu(x)
        return x
